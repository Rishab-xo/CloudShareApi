package com.app.CloudShareApi.repository;

import com.app.CloudShareApi.documents.ProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfileRepo extends MongoRepository<ProfileDocument, String> {

    Optional<ProfileDocument> findByEmail(String email);

    ProfileDocument findByClerkId(String clerkId);
    
    boolean existsByClerkId(String clerkId);

}
