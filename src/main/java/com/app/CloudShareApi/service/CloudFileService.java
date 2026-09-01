package com.app.CloudShareApi.service;

import com.app.CloudShareApi.documents.CloudFile;
import com.app.CloudShareApi.repository.CloudFileRepo;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudFileService {

    private final MinioClient minioClient;
    private final CloudFileRepo cloudFileRepo;
    // 1. Inject the credits service
    private final UserCreditsService userCreditsService;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.region}")
    private String region;

    public CloudFile uploadFile(MultipartFile file, String ownerId) throws Exception {
        // 2. Pre-check: Stop the upload immediately if they have 0 credits
        if (!userCreditsService.hasEnoughCredits(1)) {
            throw new RuntimeException("Insufficient credits to upload this file.");
        }

        String originalFileName = file.getOriginalFilename();
        String objectKey = UUID.randomUUID().toString() + "-" + originalFileName;

        // Stream the file directly into AWS S3
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        // 3. Consume the credit ONLY AFTER a successful AWS upload
        // This ensures users aren't charged for failed uploads
        userCreditsService.consumeCredit();

        // Save file metadata to MongoDB
        CloudFile cloudFile = new CloudFile();
        cloudFile.setOriginalFileName(originalFileName);
        cloudFile.setObjectKey(objectKey);
        cloudFile.setContentType(file.getContentType());
        cloudFile.setSize(file.getSize());
        cloudFile.setOwnerId(ownerId);

        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
        cloudFile.setUrl(fileUrl);

        return cloudFileRepo.save(cloudFile);
    }

    public Page<CloudFile> getUserFiles(String ownerId, int pageNo, int pageSize) {
        return cloudFileRepo.findByOwnerId(ownerId, PageRequest.of(pageNo, pageSize));
    }

    public void deleteFile(String fileId, String ownerId) throws Exception {
        CloudFile file = cloudFileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized to delete this file");
        }

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(file.getObjectKey())
                        .build()
        );

        cloudFileRepo.delete(file);
    }

    public CloudFile togglePublic(String fileId, String ownerId) throws Exception {
        CloudFile file = cloudFileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized to modify this file");
        }

        file.setPublic(!file.isPublic());
        return cloudFileRepo.save(file);
    }

    public CloudFile getPublicFile(String fileId) throws Exception {
        CloudFile file = cloudFileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.isPublic()) {
            throw new RuntimeException("This file is private and cannot be viewed");
        }

        return file;
    }
}