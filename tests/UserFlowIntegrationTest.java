package com.lazypay.integration;

import com.lazypay.PayLaterServiceApplication;
import com.lazypay.domain.User;
import com.lazypay.domain.Merchant;
import com.lazypay.domain.Transaction;
import com.lazypay.domain.Payback;
import com.lazypay.repository.UserRepository;
import com.lazypay.repository.MerchantRepository;
import com.lazypay.repository.TransactionRepository;
import com.lazypay.repository.PaybackRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = PayLaterServiceApplication.class)
@ActiveProfiles("test")
public class UserFlowIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaybackRepository paybackRepository;

    private String userApiKey;
    private String merchantApiKey;
    private String userName;
    private String merchantName;

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.deleteAll().block();
        merchantRepository.deleteAll().block();
        transactionRepository.deleteAll().block();
        paybackRepository.deleteAll().block();

        // Create test user
        User user = new User();
        user.setId("test-user-flow-1");
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCreditLimit(new BigDecimal("1000.00"));
        user.setDues(BigDecimal.ZERO);
        user.setApiKey("TEST_USER_FLOW_KEY_123");
        user = userRepository.save(user).block();
        userApiKey = user.getApiKey();
        userName = user.getName();

        // Create test merchant
        Merchant merchant = new Merchant();
        merchant.setId("test-merchant-flow-1");
        merchant.setName("Electronics Store");
        merchant.setFeePercentage(new BigDecimal("2.5"));
        merchant.setTotalFeeCollected(BigDecimal.ZERO);
        merchant.setApiKey("TEST_MERCHANT_FLOW_KEY_456");
        merchant = merchantRepository.save(merchant).block();
        merchantApiKey = merchant.getApiKey();
        merchantName = merchant.getName();
    }

    @Test
    void testCompleteUserFlow_Success() {
        // 1. Verify user exists
        webTestClient.get()
                .uri("/users/{email}", "john.doe@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.name").isEqualTo("John Doe")
                .jsonPath("$.data.creditLimit").isEqualTo("1000.00")
                .jsonPath("$.data.dues").isEqualTo("0");

        // 2. Create transaction within credit limit
        String transactionRequest = "{\"userName\":\"John Doe\",\"merchantName\":\"Electronics Store\",\"amount\":\"500.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.amount").isEqualTo("500.00")
                .jsonPath("$.data.status").isEqualTo("APPROVED");

        // 3. Verify user dues increased
        webTestClient.get()
                .uri("/users/{email}", "john.doe@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("500.00");

        // 4. Verify merchant fee collected
        webTestClient.get()
                .uri("/reports/fee/{merchantName}", merchantName)
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEqualTo("12.50"); // 2.5% of 500.00

        // 5. Create payback to reduce dues
        String paybackRequest = "{\"userName\":\"John Doe\",\"amount\":\"200.00\"}";
        
        webTestClient.post()
                .uri("/paybacks")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(paybackRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.amount").isEqualTo("200.00")
                .jsonPath("$.data.remainingDues").isEqualTo("300.00");

        // 6. Verify user dues reduced
        webTestClient.get()
                .uri("/users/{email}", "john.doe@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("300.00");

        // 7. Check total dues report
        webTestClient.get()
                .uri("/reports/total-dues")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.totalDues").isEqualTo("300.00")
                .jsonPath("$.data.userCount").isEqualTo(1);
    }

    @Test
    void testTransactionExceedsCreditLimit_Rejected() {
        // Try to create transaction exceeding credit limit
        String transactionRequest = "{\"userName\":\"John Doe\",\"merchantName\":\"Electronics Store\",\"amount\":\"1500.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.reason").value(reason -> reason.toString().contains("Credit limit exceeded"));

        // Verify user dues unchanged
        webTestClient.get()
                .uri("/users/{email}", "john.doe@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("0");
    }

    @Test
    void testUserAtCreditLimit_ReportedCorrectly() {
        // Create transaction to reach credit limit
        String transactionRequest = "{\"userName\":\"John Doe\",\"merchantName\":\"Electronics Store\",\"amount\":\"1000.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated();

        // Check users at credit limit report
        webTestClient.get()
                .uri("/reports/users-at-credit-limit")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].name").isEqualTo("John Doe")
                .jsonPath("$.data[0].dues").isEqualTo("1000.00")
                .jsonPath("$.data[0].creditLimit").isEqualTo("1000.00");
    }
}
