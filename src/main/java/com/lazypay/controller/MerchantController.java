package com.lazypay.controller;

import com.lazypay.domain.Merchant;
import com.lazypay.dto.ApiResponse;
import com.lazypay.dto.MerchantOnboardRequest;
import com.lazypay.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Merchant>>> onboardMerchant(@Valid @RequestBody MerchantOnboardRequest request) {
        return merchantService.onboardMerchant(request)
                .map(merchant -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Merchant onboarded successfully", merchant)));
    }

    @PatchMapping("/{merchantName}")
    public Mono<ResponseEntity<ApiResponse<Merchant>>> updateFeePercentage(
            @PathVariable String merchantName,
            @RequestParam BigDecimal feePercentage) {
        return merchantService.updateFeePercentage(merchantName, feePercentage)
                .map(merchant -> ResponseEntity.ok(ApiResponse.success("Fee percentage updated successfully", merchant)));
    }

    @GetMapping("/{merchantName}")
    public Mono<ResponseEntity<ApiResponse<Merchant>>> getMerchantByName(@PathVariable String merchantName) {
        return merchantService.getMerchantByName(merchantName)
                .map(merchant -> ResponseEntity.ok(ApiResponse.success("Merchant retrieved successfully", merchant)));
    }
}
