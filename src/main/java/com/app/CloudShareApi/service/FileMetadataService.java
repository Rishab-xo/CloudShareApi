package com.app.CloudShareApi.service;

import com.app.CloudShareApi.documents.FileMetaDataDocument;
import com.app.CloudShareApi.documents.ProfileDocument;
import com.app.CloudShareApi.dto.FileMetaDataDTO;
import com.app.CloudShareApi.repository.FileMetadataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataRepo fileMetadataRepo;

    public List<FileMetaDataDTO> uploadFiles(MultipartFile files[]) throws IOException {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        List<FileMetaDataDocument> savedFiles = new ArrayList<>();

        if (!userCreditsService.hasEnoughCredits(files.length)){
            throw new RuntimeException("Not enough credits too upload files. please purchase");
        }

        Path uploadPath = Paths.get("upload").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        for (MultipartFile file : files){
            String fileName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileMetaDataDocument fileMetadata = FileMetaDataDocument.builder()
                    .fileLocation(targetLocation.toString())
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(currentProfile.getClerkId())
                    .isPublic(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            userCreditsService.consumeCredit();

            savedFiles.add(fileMetadataRepo.save(fileMetadata));
        }

     return savedFiles.stream().map(fileMetaDataDocument -> mapToDTO(fileMetaDataDocument))
                .collect(Collectors.toList());

    }

    private FileMetaDataDTO mapToDTO(FileMetaDataDocument fileMetaDataDocument) {
        return FileMetaDataDTO.builder()
                .id(fileMetaDataDocument.getId())
                .fileLocation(fileMetaDataDocument.getFileLocation())
                .name(fileMetaDataDocument.getName())
                .size(fileMetaDataDocument.getSize())
                .type(fileMetaDataDocument.getType())
                .clerkId(fileMetaDataDocument.getClerkId())
                .isPublic(fileMetaDataDocument.getIsPublic())
                .uploadedAt(fileMetaDataDocument.getUploadedAt())
                .build();
    }

    public List<FileMetaDataDTO> getFiles(){

        ProfileDocument currentProfile = profileService.getCurrentProfile();
        List<FileMetaDataDocument> files = fileMetadataRepo.findByClerkId(currentProfile.getClerkId());

        return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FileMetaDataDTO getPublicFile(String id){
        Optional<FileMetaDataDocument> fileOptional = fileMetadataRepo.findById(id);
        if (fileOptional.isEmpty() || !fileOptional.get().getIsPublic()){
            throw new RuntimeException("Unable to get the file");
        }

        FileMetaDataDocument document = fileOptional.get();
        return mapToDTO(document);
    }

    public FileMetaDataDTO getDownloadableFile(String id){
        FileMetaDataDocument file = fileMetadataRepo.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
        return mapToDTO(file);
    }

    public void deleteFile(String id){
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            FileMetaDataDocument file = fileMetadataRepo.findById(id).orElseThrow(()-> new RuntimeException("File not found"));

            if (!file.getClerkId().equals(currentProfile.getClerkId())){
                throw new RuntimeException("File does not belong to Current User");
            }

            Path path = Paths.get(file.getFileLocation());
            Files.deleteIfExists(path);
            fileMetadataRepo.deleteById(id);

        }
        catch (Exception e){
            throw new RuntimeException("Error deleting the File");
        }
    }

    public FileMetaDataDTO togglePublic(String id){

        FileMetaDataDocument file = fileMetadataRepo.findById(id).orElseThrow(()-> new RuntimeException("File not found"));

        file.setIsPublic(!file.getIsPublic());
        fileMetadataRepo.save(file);
        return mapToDTO(file);
    }

}
