package com.app.CloudShareApi.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "cloud_files")
@Data
public class CloudFile {

    @Id
    private String id;

    private String originalFileName;
    private String objectKey;
    private String url;
    private String contentType;
    private Long size;
    private String ownerId;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    private boolean isPublic = false;
}
