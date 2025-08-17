package com.lazypay.controller;

import com.lazypay.domain.Payback;
import com.lazypay.dto.ApiResponse;
import com.lazypay.dto.PaybackRequest;
import com.lazypay.service.PaybackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/paybacks")
public class PaybackController {

    @Autowired
    private PaybackService paybackService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Payback>>> recordPayback(@Valid @RequestBody PaybackRequest request) {
        return paybackService.recordPayback(request)
                .map(payback -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Payback recorded successfully", payback)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Payback>>> getPaybackById(@PathVariable String id) {
        return paybackService.getPaybackById(id)
                .map(payback -> ResponseEntity.ok(ApiResponse.success("Payback retrieved successfully", payback)));
    }
}
