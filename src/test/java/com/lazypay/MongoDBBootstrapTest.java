package com.lazypay;

import com.lazypay.domain.User;
import com.lazypay.domain.Merchant;
import com.lazypay.repository.UserRepository;
import com.lazypay.repository.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MongoDBBootstrapTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Test
    public void testMongoDBConnectionAndInsertUser() {
        // Create a sample user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCreditLimit(new BigDecimal("1000.00"));
        user.setDues(new BigDecimal("0.00"));

        // Insert user
        Mono<User> savedUser = userRepository.save(user);
        
        StepVerifier.create(savedUser)
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals("John Doe", saved.getName());
                    assertEquals("john.doe@example.com", saved.getEmail());
                    assertEquals(0, saved.getCreditLimit().compareTo(new BigDecimal("1000.00")));
                    assertEquals(0, saved.getDues().compareTo(new BigDecimal("0.00")));
                    System.out.println("✅ User inserted successfully: " + saved);
                })
                .verifyComplete();
    }

    @Test
    public void testMongoDBConnectionAndInsertMerchant() {
        // Create a sample merchant
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID().toString());
        merchant.setName("TechStore Inc");
        merchant.setFeePercentage(new BigDecimal("2.5"));
        merchant.setTotalFeeCollected(new BigDecimal("0.00"));

        // Insert merchant
        Mono<Merchant> savedMerchant = merchantRepository.save(merchant);
        
        StepVerifier.create(savedMerchant)
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals("TechStore Inc", saved.getName());
                    assertEquals(0, saved.getFeePercentage().compareTo(new BigDecimal("2.5")));
                    assertEquals(0, saved.getTotalFeeCollected().compareTo(new BigDecimal("0.00")));
                    System.out.println("✅ Merchant inserted successfully: " + saved);
                })
                .verifyComplete();
    }

    @Test
    public void testRetrieveUserAndMerchant() {
        // First insert a user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName("Jane Smith");
        user.setEmail("jane.smith@example.com");
        user.setCreditLimit(new BigDecimal("2000.00"));
        user.setDues(new BigDecimal("150.00"));

        // Insert user and then retrieve it
        Mono<User> savedUser = userRepository.save(user)
                .flatMap(saved -> userRepository.findById(saved.getId()));

        StepVerifier.create(savedUser)
                .assertNext(retrieved -> {
                    assertEquals("Jane Smith", retrieved.getName());
                    assertEquals("jane.smith@example.com", retrieved.getEmail());
                    System.out.println("✅ User retrieved successfully: " + retrieved);
                })
                .verifyComplete();

        // First insert a merchant
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID().toString());
        merchant.setName("Fashion Outlet");
        merchant.setFeePercentage(new BigDecimal("3.0"));
        merchant.setTotalFeeCollected(new BigDecimal("25.50"));

        // Insert merchant and then retrieve it
        Mono<Merchant> savedMerchant = merchantRepository.save(merchant)
                .flatMap(saved -> merchantRepository.findById(saved.getId()));

        StepVerifier.create(savedMerchant)
                .assertNext(retrieved -> {
                    assertEquals("Fashion Outlet", retrieved.getName());
                    assertEquals(0, retrieved.getFeePercentage().compareTo(new BigDecimal("3.0")));
                    System.out.println("✅ Merchant retrieved successfully: " + retrieved);
                })
                .verifyComplete();
    }
}
