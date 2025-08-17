package com.lazypay.repository;

import com.lazypay.domain.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    
    /**
     * Find transactions by user name
     */
    Flux<Transaction> findByUserName(String userName);
    
    /**
     * Find transactions by merchant name
     */
    Flux<Transaction> findByMerchantName(String merchantName);
    
    /**
     * Find transactions by status
     */
    Flux<Transaction> findByStatus(String status);
    
    /**
     * Find transactions by user name and status
     */
    Flux<Transaction> findByUserNameAndStatus(String userName, String status);
}
