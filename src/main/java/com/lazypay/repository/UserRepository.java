package com.lazypay.repository;

import com.lazypay.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    
    /**
     * Find user by email
     */
    Mono<User> findByEmail(String email);
    
    /**
     * Find user by name
     */
    Mono<User> findByName(String name);
    
    /**
     * Check if user exists by email
     */
    Mono<Boolean> existsByEmail(String email);
    
    Mono<User> findByApiKey(String apiKey);
}
