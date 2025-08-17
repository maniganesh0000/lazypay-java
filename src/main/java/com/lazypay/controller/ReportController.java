package com.lazypay.controller;

import com.lazypay.domain.User;
import com.lazypay.dto.ApiResponse;
import com.lazypay.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/fee/{merchantName}")
    public Mono<ResponseEntity<ApiResponse<BigDecimal>>> getTotalFeeCollected(@PathVariable String merchantName) {
        return reportService.getTotalFeeCollected(merchantName)
                .map(fee -> ResponseEntity.ok(ApiResponse.success("Total fee collected", fee)));
    }

    @GetMapping("/dues/{userName}")
    public Mono<ResponseEntity<ApiResponse<BigDecimal>>> getDuesForUser(@PathVariable String userName) {
        return reportService.getDuesForUser(userName)
                .map(dues -> ResponseEntity.ok(ApiResponse.success("Dues for user", dues)));
    }

    @GetMapping("/users-at-credit-limit")
    public Mono<ResponseEntity<ApiResponse<Flux<User>>>> getUsersAtCreditLimit() {
        Flux<User> users = reportService.getUsersAtCreditLimit();
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Users at credit limit", users)));
    }

    @GetMapping("/total-dues")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getTotalDuesBreakdown() {
        return reportService.getTotalDuesBreakdown()
                .map(breakdown -> ResponseEntity.ok(ApiResponse.success("Total dues breakdown", breakdown)));
    }
}
