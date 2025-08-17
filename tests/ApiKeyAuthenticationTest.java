package com.lazypay.security;

import com.lazypay.PayLaterServiceApplication;
import com.lazypay.domain.User;
import com.lazypay.domain.Merchant;
import com.lazypay.repository.UserRepository;
import com.lazypay.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = PayLaterServiceApplication.class)
@ActiveProfiles("test")
public class ApiKeyAuthenticationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    private User testUser;
    private Merchant testMerchant;
    private String userApiKey;
    private String merchantApiKey;

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.deleteAll().block();
        merchantRepository.deleteAll().block();

        // Create test user
        testUser = new User();
        testUser.setId("test-user-1");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setCreditLimit(new BigDecimal("1000.00"));
        testUser.setDues(BigDecimal.ZERO);
        testUser.setApiKey("TEST_USER_API_KEY_123");
        testUser = userRepository.save(testUser).block();
        userApiKey = testUser.getApiKey();

        // Create test merchant
        testMerchant = new Merchant();
        testMerchant.setId("test-merchant-1");
        testMerchant.setName("Test Merchant");
        testMerchant.setFeePercentage(new BigDecimal("2.5"));
        testMerchant.setTotalFeeCollected(BigDecimal.ZERO);
        testMerchant.setApiKey("TEST_MERCHANT_API_KEY_456");
        testMerchant = merchantRepository.save(testMerchant).block();
        merchantApiKey = testMerchant.getApiKey();
    }

    @Test
    void testHealthEndpointWithoutApiKey_ShouldSucceed() {
        webTestClient.get()
                .uri("/test/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success");
    }

    @Test
    void testUserEndpointWithoutApiKey_ShouldFail() {
        webTestClient.get()
                .uri("/users/test@example.com")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.reason").isEqualTo("Missing API key");
    }

    @Test
    void testUserEndpointWithValidApiKey_ShouldSucceed() {
        webTestClient.get()
                .uri("/users/test@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("Test User");
    }

    @Test
    void testUserEndpointWithInvalidApiKey_ShouldFail() {
        webTestClient.get()
                .uri("/users/test@example.com")
                .header("X-API-KEY", "INVALID_API_KEY")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.reason").isEqualTo("Invalid API key");
    }

    @Test
    void testMerchantEndpointWithValidApiKey_ShouldSucceed() {
        webTestClient.get()
                .uri("/merchants/Test Merchant")
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("Test Merchant");
    }

    @Test
    void testCreateUserWithValidApiKey_ShouldSucceed() {
        String newUserJson = "{\"name\":\"New User\",\"email\":\"newuser@example.com\",\"creditLimit\":\"500.00\"}";

        webTestClient.post()
                .uri("/users")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(BodyInserters.fromValue(newUserJson))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("New User");
    }

    @Test
    void testCreateUserWithoutApiKey_ShouldFail() {
        String newUserJson = "{\"name\":\"New User\",\"email\":\"newuser@example.com\",\"creditLimit\":\"500.00\"}";

        webTestClient.post()
                .uri("/users")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(BodyInserters.fromValue(newUserJson))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.reason").isEqualTo("Missing API key");
    }
}
