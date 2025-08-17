package com.lazypay.integration;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = PayLaterServiceApplication.class)
@ActiveProfiles("test")
public class TransactionPaybackTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    private String userApiKey;
    private String merchantApiKey;
    private String userName;
    private String merchantName;

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.deleteAll().block();
        merchantRepository.deleteAll().block();

        // Create test user
        User user = new User();
        user.setId("test-user-txn-1");
        user.setName("Transaction User");
        user.setEmail("txn.user@example.com");
        user.setCreditLimit(new BigDecimal("2000.00"));
        user.setDues(BigDecimal.ZERO);
        user.setApiKey("TEST_USER_TXN_KEY_404");
        user = userRepository.save(user).block();
        userApiKey = user.getApiKey();
        userName = user.getName();

        // Create test merchant
        Merchant merchant = new Merchant();
        merchant.setId("test-merchant-txn-1");
        merchant.setName("Transaction Store");
        merchant.setFeePercentage(new BigDecimal("3.0"));
        merchant.setTotalFeeCollected(BigDecimal.ZERO);
        merchant.setApiKey("TEST_MERCHANT_TXN_KEY_505");
        merchant = merchantRepository.save(merchant).block();
        merchantApiKey = merchant.getApiKey();
        merchantName = merchant.getName();
    }

    @Test
    void testTransactionSuccess_WithinCreditLimit() {
        String transactionRequest = "{\"userName\":\"Transaction User\",\"merchantName\":\"Transaction Store\",\"amount\":\"1000.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.amount").isEqualTo("1000.00")
                .jsonPath("$.data.status").isEqualTo("APPROVED")
                .jsonPath("$.data.feeAmount").isEqualTo("30.00"); // 3.0% of 1000.00

        // Verify user dues increased
        webTestClient.get()
                .uri("/users/{email}", "txn.user@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("1000.00");

        // Verify merchant fee collected
        webTestClient.get()
                .uri("/reports/fee/{merchantName}", merchantName)
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEqualTo("30.00");
    }

    @Test
    void testTransactionFailure_ExceedsCreditLimit() {
        String transactionRequest = "{\"userName\":\"Transaction User\",\"merchantName\":\"Transaction Store\",\"amount\":\"2500.00\"}";
        
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
                .uri("/users/{email}", "txn.user@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("0");
    }

    @Test
    void testTransactionFailure_UserNotFound() {
        String transactionRequest = "{\"userName\":\"NonExistent User\",\"merchantName\":\"Transaction Store\",\"amount\":\"500.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }

    @Test
    void testTransactionFailure_MerchantNotFound() {
        String transactionRequest = "{\"userName\":\"Transaction User\",\"merchantName\":\"NonExistent Store\",\"amount\":\"500.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }

    @Test
    void testPaybackSuccess_ReducesDues() {
        // First create a transaction to generate dues
        String transactionRequest = "{\"userName\":\"Transaction User\",\"merchantName\":\"Transaction Store\",\"amount\":\"800.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated();

        // Verify initial dues
        webTestClient.get()
                .uri("/users/{email}", "txn.user@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("800.00");

        // Create payback
        String paybackRequest = "{\"userName\":\"Transaction User\",\"amount\":\"300.00\"}";
        
        webTestClient.post()
                .uri("/paybacks")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(paybackRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.amount").isEqualTo("300.00")
                .jsonPath("$.data.remainingDues").isEqualTo("500.00");

        // Verify dues reduced
        webTestClient.get()
                .uri("/users/{email}", "txn.user@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("500.00");
    }

    @Test
    void testPaybackSuccess_ExceedsDues() {
        // First create a transaction to generate dues
        String transactionRequest = "{\"userName\":\"Transaction User\",\"merchantName\":\"Transaction Store\",\"amount\":\"600.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated();

        // Create payback that exceeds dues
        String paybackRequest = "{\"userName\":\"Transaction User\",\"amount\":\"1000.00\"}";
        
        webTestClient.post()
                .uri("/paybacks")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(paybackRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.amount").isEqualTo("1000.00")
                .jsonPath("$.data.remainingDues").isEqualTo("0");

        // Verify dues are zero (not negative)
        webTestClient.get()
                .uri("/users/{email}", "txn.user@example.com")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.dues").isEqualTo("0");
    }

    @Test
    void testPaybackFailure_UserNotFound() {
        String paybackRequest = "{\"userName\":\"NonExistent User\",\"amount\":\"100.00\"}";
        
        webTestClient.post()
                .uri("/paybacks")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(paybackRequest))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }
}
