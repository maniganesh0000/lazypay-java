package com.lazypay.integration;

import com.lazypay.PayLaterServiceApplication;
import com.lazypay.domain.User;
import com.lazypay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = PayLaterServiceApplication.class)
@ActiveProfiles("test")
public class UserOperationsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    private String userApiKey;

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.deleteAll().block();

        // Create test user
        User user = new User();
        user.setId("test-user-ops-1");
        user.setName("Test User");
        user.setEmail("test.user@example.com");
        user.setCreditLimit(new BigDecimal("2000.00"));
        user.setDues(BigDecimal.ZERO);
        user.setApiKey("TEST_USER_OPS_KEY_101");
        user = userRepository.save(user).block();
        userApiKey = user.getApiKey();
    }

    @Test
    void testUserOnboarding_Success() {
        String newUserRequest = "{\"name\":\"Jane Smith\",\"email\":\"jane.smith@example.com\",\"creditLimit\":\"1500.00\"}";
        
        webTestClient.post()
                .uri("/users")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(newUserRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("Jane Smith")
                .jsonPath("$.data.email").isEqualTo("jane.smith@example.com")
                .jsonPath("$.data.creditLimit").isEqualTo("1500.00")
                .jsonPath("$.data.dues").isEqualTo("0")
                .jsonPath("$.data.apiKey").exists();
    }

    @Test
    void testUserOnboarding_InvalidEmail() {
        String invalidUserRequest = "{\"name\":\"Invalid User\",\"email\":\"invalid-email\",\"creditLimit\":\"1000.00\"}";
        
        webTestClient.post()
                .uri("/users")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(invalidUserRequest))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }

    @Test
    void testUserOnboarding_InvalidCreditLimit() {
        String invalidUserRequest = "{\"name\":\"Invalid User\",\"email\":\"invalid@example.com\",\"creditLimit\":\"-100.00\"}";
        
        webTestClient.post()
                .uri("/users")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(invalidUserRequest))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }

    @Test
    void testUserRetrieval_Success() {
        webTestClient.get()
                .uri("/users/{email}", "test.user@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("Test User")
                .jsonPath("$.data.email").isEqualTo("test.user@example.com")
                .jsonPath("$.data.creditLimit").isEqualTo("2000.00")
                .jsonPath("$.data.dues").isEqualTo("0");
    }

    @Test
    void testUserRetrieval_NotFound() {
        webTestClient.get()
                .uri("/users/{email}", "nonexistent@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }

    @Test
    void testUserOnboarding_MissingRequiredFields() {
        String incompleteUserRequest = "{\"name\":\"Incomplete User\"}";
        
        webTestClient.post()
                .uri("/users")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(incompleteUserRequest))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }
}
