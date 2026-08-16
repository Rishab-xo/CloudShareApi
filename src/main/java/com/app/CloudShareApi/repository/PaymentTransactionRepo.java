package com.app.CloudShareApi.repository;


import com.app.CloudShareApi.documents.PaymentTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepo extends MongoRepository<PaymentTransaction, String > {

    List<PaymentTransaction> findByClerkId(String clerkId);

    List<PaymentTransaction> findByClerkIdOrderByTransactionDate(String clerkId);

    List<PaymentTransaction> findByClerkIdAndStatusOrderByTransactionDateDesc(String clerkId, String status);

    Optional<PaymentTransaction> findByOrderId(String orderId);
}
