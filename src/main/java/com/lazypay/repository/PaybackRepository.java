package com.lazypay.repository;

import com.lazypay.domain.Payback;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PaybackRepository extends ReactiveMongoRepository<Payback, String> {
    
    /**
     * Find paybacks by user name
     */
    Flux<Payback> findByUserName(String userName);
    
    /**
     * Find paybacks by user name ordered by creation date
     */
    Flux<Payback> findByUserNameOrderByCreatedAtDesc(String userName);
}
