package com.app.CloudShareApi.controller;

import com.app.CloudShareApi.documents.CloudFile;
import com.app.CloudShareApi.documents.UserCredits;
import com.app.CloudShareApi.service.CloudFileService;
import com.app.CloudShareApi.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final UserCreditsService userCreditsService;
    private final CloudFileService cloudFileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestPart MultipartFile files[], java.security.Principal principal) throws Exception {
        Map response = new HashMap<>();

        // 1. Get the secure User ID from the JWT token
        String ownerId = principal.getName();

        // 2. Upload every file to MinIO using our new service
        List<CloudFile> uploadedFiles = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            CloudFile savedFile = cloudFileService.uploadFile(file, ownerId);
            uploadedFiles.add(savedFile);
        }

        // 3. Keep your existing credits logic intact
        UserCredits FinalCredits = userCreditsService.getUserCredits();

        response.put("files", uploadedFiles);
        response.put("remainingCredits", FinalCredits);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getFilesForCurrentUser(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            Principal principal) {

        String ownerId = principal.getName();
        Page paginatedFiles = cloudFileService.getUserFiles(ownerId, pageNo, pageSize);
        return ResponseEntity.ok(paginatedFiles);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id) {
        try {
            CloudFile file = cloudFileService.getPublicFile(id);
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity togglePublic(@PathVariable String id, java.security.Principal principal) {
        try {
            // Get the secure user ID from the token
            String ownerId = principal.getName();

            CloudFile updatedFile = cloudFileService.togglePublic(id, ownerId);
            return ResponseEntity.ok(updatedFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not toggle status: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id, Principal principal) {
        try {
            // Grab the secure user ID and pass it to the service to delete the file
            cloudFileService.deleteFile(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not delete file: " + e.getMessage());
        }
    }

}
