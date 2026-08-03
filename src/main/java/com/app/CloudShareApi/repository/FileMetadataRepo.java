package com.app.CloudShareApi.repository;

import com.app.CloudShareApi.documents.FileMetaDataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetadataRepo extends MongoRepository<FileMetaDataDocument, String> {

    List<FileMetaDataDocument> findByClerkId(String clerkId);

    Long countByClerkId(String clerkId);

}
