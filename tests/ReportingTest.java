package com.lazypay.integration;

import com.lazypay.PayLaterServiceApplication;
import com.lazypay.domain.User;
import com.lazypay.domain.Merchant;
import com.lazypay.domain.Transaction;
import com.lazypay.repository.UserRepository;
import com.lazypay.repository.MerchantRepository;
import com.lazypay.repository.TransactionRepository;
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
public class ReportingTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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

        // Create test user
        User user = new User();
        user.setId("test-user-report-1");
        user.setName("Report User");
        user.setEmail("report.user@example.com");
        user.setCreditLimit(new BigDecimal("3000.00"));
        user.setDues(BigDecimal.ZERO);
        user.setApiKey("TEST_USER_REPORT_KEY_202");
        user = userRepository.save(user).block();
        userApiKey = user.getApiKey();
        userName = user.getName();

        // Create test merchant
        Merchant merchant = new Merchant();
        merchant.setId("test-merchant-report-1");
        merchant.setName("Report Store");
        merchant.setFeePercentage(new BigDecimal("5.0"));
        merchant.setTotalFeeCollected(BigDecimal.ZERO);
        merchant.setApiKey("TEST_MERCHANT_REPORT_KEY_303");
        merchant = merchantRepository.save(merchant).block();
        merchantApiKey = merchant.getApiKey();
        merchantName = merchant.getName();
    }

    @Test
    void testFeeCollectionReport_Success() {
        // Create a transaction to generate fees
        String transactionRequest = "{\"userName\":\"Report User\",\"merchantName\":\"Report Store\",\"amount\":\"1000.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated();

        // Check fee collection report
        webTestClient.get()
                .uri("/reports/fee/{merchantName}", merchantName)
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data").isEqualTo("50.00"); // 5.0% of 1000.00
    }

    @Test
    void testDuesReport_Success() {
        // Create a transaction to generate dues
        String transactionRequest = "{\"userName\":\"Report User\",\"merchantName\":\"Report Store\",\"amount\":\"800.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transactionRequest))
                .exchange()
                .expectStatus().isCreated();

        // Check dues report
        webTestClient.get()
                .uri("/reports/dues/{userName}", userName)
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data").isEqualTo("800.00");
    }

    @Test
    void testUsersAtCreditLimitReport_Success() {
        // Create a transaction to reach credit limit
        String transactionRequest = "{\"userName\":\"Report User\",\"merchantName\":\"Report Store\",\"amount\":\"3000.00\"}";
        
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
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data[0].name").isEqualTo("Report User")
                .jsonPath("$.data[0].dues").isEqualTo("3000.00")
                .jsonPath("$.data[0].creditLimit").isEqualTo("3000.00");
    }

    @Test
    void testTotalDuesReport_Success() {
        // Create multiple transactions
        String transaction1 = "{\"userName\":\"Report User\",\"merchantName\":\"Report Store\",\"amount\":\"500.00\"}";
        String transaction2 = "{\"userName\":\"Report User\",\"merchantName\":\"Report Store\",\"amount\":\"300.00\"}";
        
        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transaction1))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/transactions")
                .header("X-API-KEY", userApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(transaction2))
                .exchange()
                .expectStatus().isCreated();

        // Check total dues report
        webTestClient.get()
                .uri("/reports/total-dues")
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.totalDues").isEqualTo("800.00")
                .jsonPath("$.data.totalCreditLimit").isEqualTo("3000.00")
                .jsonPath("$.data.totalAvailableCredit").isEqualTo("2200.00")
                .jsonPath("$.data.userCount").isEqualTo(1)
                .jsonPath("$.data.usersAtCreditLimit").isEqualTo(0);
    }

    @Test
    void testFeeCollectionReport_NoTransactions() {
        // Check fee collection report when no transactions exist
        webTestClient.get()
                .uri("/reports/fee/{merchantName}", merchantName)
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data").isEqualTo("0");
    }

    @Test
    void testDuesReport_NoDues() {
        // Check dues report when no dues exist
        webTestClient.get()
                .uri("/reports/dues/{userName}", userName)
                .header("X-API-KEY", userApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data").isEqualTo("0");
    }
}
