package com.lazypay.controller;

import com.lazypay.dto.ApiResponse;
import com.lazypay.service.UserService;
import com.lazypay.service.MerchantService;
import com.lazypay.service.TransactionService;
import com.lazypay.service.PaybackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PaybackService paybackService;

    @GetMapping("/health")
    public Mono<ResponseEntity<ApiResponse<String>>> healthCheck() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Pay-Later Service is running!", "OK")));
    }

    @PostMapping("/bootstrap")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> bootstrapTestData() {
        Map<String, String> results = new HashMap<>();
        
        // This will be implemented to create test data
        results.put("status", "Test data creation initiated");
        results.put("message", "Use individual endpoints to test functionality");
        
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Bootstrap test initiated", results)));
    }
}
