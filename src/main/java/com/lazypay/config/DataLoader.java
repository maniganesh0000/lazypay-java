package com.lazypay.config;

import com.lazypay.domain.User;
import com.lazypay.domain.Merchant;
import com.lazypay.repository.UserRepository;
import com.lazypay.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting Pay-Later Service...");
        System.out.println("📊 Bootstrap test: Inserting sample data...");
        
        // Test User insertion
        User testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setCreditLimit(new BigDecimal("1000.00"));
        testUser.setDues(new BigDecimal("0.00"));

        userRepository.save(testUser)
                .doOnSuccess(user -> {
                    System.out.println("✅ User inserted successfully: " + user.getName());
                    System.out.println("   ID: " + user.getId());
                    System.out.println("   Email: " + user.getEmail());
                    System.out.println("   Credit Limit: " + user.getCreditLimit());
                    System.out.println("   Dues: " + user.getDues());
                })
                .doOnError(error -> {
                    System.err.println("❌ Error inserting user: " + error.getMessage());
                })
                .subscribe();

        // Test Merchant insertion
        Merchant testMerchant = new Merchant();
        testMerchant.setId(UUID.randomUUID().toString());
        testMerchant.setName("Test Merchant");
        testMerchant.setFeePercentage(new BigDecimal("2.5"));
        testMerchant.setTotalFeeCollected(new BigDecimal("0.00"));

        merchantRepository.save(testMerchant)
                .doOnSuccess(merchant -> {
                    System.out.println("✅ Merchant inserted successfully: " + merchant.getName());
                    System.out.println("   ID: " + merchant.getId());
                    System.out.println("   Fee Percentage: " + merchant.getFeePercentage() + "%");
                    System.out.println("   Total Fee Collected: " + merchant.getTotalFeeCollected());
                })
                .doOnError(error -> {
                    System.err.println("❌ Error inserting merchant: " + error.getMessage());
                })
                .subscribe();

        // Test retrieval
        userRepository.findByEmail("test@example.com")
                .doOnSuccess(user -> {
                    if (user != null) {
                        System.out.println("✅ User retrieved successfully: " + user.getName());
                    } else {
                        System.out.println("⚠️  User not found");
                    }
                })
                .doOnError(error -> {
                    System.err.println("❌ Error retrieving user: " + error.getMessage());
                })
                .subscribe();

        merchantRepository.findByName("Test Merchant")
                .doOnSuccess(merchant -> {
                    if (merchant != null) {
                        System.out.println("✅ Merchant retrieved successfully: " + merchant.getName());
                    } else {
                        System.out.println("⚠️  Merchant not found");
                    }
                })
                .doOnError(error -> {
                    System.err.println("❌ Error retrieving merchant: " + error.getMessage());
                })
                .subscribe();

        System.out.println("🎯 Bootstrap test completed!");
    }
}
