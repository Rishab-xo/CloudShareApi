package com.app.CloudShareApi.repository;

import com.app.CloudShareApi.documents.UserCredits;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserCreditsRepo extends MongoRepository<UserCredits, String> {

        boolean existsByClerkId(String clerkId);
        Optional<UserCredits> findByClerkId(String clerkId);

}
