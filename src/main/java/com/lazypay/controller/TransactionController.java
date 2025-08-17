package com.lazypay.controller;

import com.lazypay.domain.Transaction;
import com.lazypay.dto.ApiResponse;
import com.lazypay.dto.TransactionRequest;
import com.lazypay.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Transaction>>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        return transactionService.createTransaction(request)
                .map(transaction -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Transaction created successfully", transaction)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Transaction>>> getTransactionById(@PathVariable String id) {
        return transactionService.getTransactionById(id)
                .map(transaction -> ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", transaction)));
    }
}
