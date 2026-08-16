package com.app.CloudShareApi.repository;

import com.app.CloudShareApi.documents.CloudFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudFileRepo extends MongoRepository<CloudFile, String> {

    Page findByOwnerId(String ownerId, Pageable pageable);

}
