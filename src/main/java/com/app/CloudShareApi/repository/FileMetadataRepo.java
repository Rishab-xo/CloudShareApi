package com.app.CloudShareApi.repository;

import com.app.CloudShareApi.documents.FileMetaDataDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetadataRepo extends MongoRepository<FileMetaDataDocument, String> {


    Long countByClerkId(String clerkId);

    Page<FileMetaDataDocument> findByClerkId(String clerkId, Pageable pageable);

}
