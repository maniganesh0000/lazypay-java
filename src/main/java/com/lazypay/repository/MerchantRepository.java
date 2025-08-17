package com.lazypay.repository;

import com.lazypay.domain.Merchant;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MerchantRepository extends ReactiveMongoRepository<Merchant, String> {
    
    /**
     * Find merchant by name
     */
    Mono<Merchant> findByName(String name);
    
    /**
     * Check if merchant exists by name
     */
    Mono<Boolean> existsByName(String name);
    
    Mono<Merchant> findByApiKey(String apiKey);
}
