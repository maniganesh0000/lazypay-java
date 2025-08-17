package com.lazypay.integration;

import com.lazypay.PayLaterServiceApplication;
import com.lazypay.domain.Merchant;
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
public class MerchantOperationsTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MerchantRepository merchantRepository;

    private String merchantApiKey;

    @BeforeEach
    void setUp() {
        // Clean up test data
        merchantRepository.deleteAll().block();

        // Create test merchant
        Merchant merchant = new Merchant();
        merchant.setId("test-merchant-ops-1");
        merchant.setName("Test Store");
        merchant.setFeePercentage(new BigDecimal("3.0"));
        merchant.setTotalFeeCollected(BigDecimal.ZERO);
        merchant.setApiKey("TEST_MERCHANT_OPS_KEY_789");
        merchant = merchantRepository.save(merchant).block();
        merchantApiKey = merchant.getApiKey();
    }

    @Test
    void testMerchantOnboarding_Success() {
        String newMerchantRequest = "{\"name\":\"New Electronics Store\",\"feePercentage\":\"2.0\"}";
        
        webTestClient.post()
                .uri("/merchants")
                .header("X-API-KEY", merchantApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(newMerchantRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("New Electronics Store")
                .jsonPath("$.data.feePercentage").isEqualTo("2.0")
                .jsonPath("$.data.totalFeeCollected").isEqualTo("0")
                .jsonPath("$.data.apiKey").exists();
    }

    @Test
    void testMerchantFeeUpdate_Success() {
        // Update merchant fee percentage
        webTestClient.patch()
                .uri("/merchants/{merchantName}", "Test Store")
                .header("X-API-KEY", merchantApiKey)
                .queryParam("feePercentage", "4.5")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.feePercentage").isEqualTo("4.5");

        // Verify the update
        webTestClient.get()
                .uri("/merchants/{merchantName}", "Test Store")
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.feePercentage").isEqualTo("4.5");
    }

    @Test
    void testMerchantFeeUpdate_InvalidPercentage() {
        // Try to update with invalid fee percentage (over 100%)
        webTestClient.patch()
                .uri("/merchants/{merchantName}", "Test Store")
                .header("X-API-KEY", merchantApiKey)
                .queryParam("feePercentage", "150.0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }

    @Test
    void testMerchantRetrieval_Success() {
        webTestClient.get()
                .uri("/merchants/{merchantName}", "Test Store")
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.name").isEqualTo("Test Store")
                .jsonPath("$.data.feePercentage").isEqualTo("3.0")
                .jsonPath("$.data.totalFeeCollected").isEqualTo("0");
    }

    @Test
    void testMerchantRetrieval_NotFound() {
        webTestClient.get()
                .uri("/merchants/{merchantName}", "NonExistentStore")
                .header("X-API-KEY", merchantApiKey)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error");
    }
}
