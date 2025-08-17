package com.lazypay.controller;

import com.lazypay.domain.User;
import com.lazypay.dto.ApiResponse;
import com.lazypay.dto.UserOnboardRequest;
import com.lazypay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<User>>> onboardUser(@Valid @RequestBody UserOnboardRequest request) {
        return userService.onboardUser(request)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("User onboarded successfully", user)));
    }

    @GetMapping("/{email}")
    public Mono<ResponseEntity<ApiResponse<User>>> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user)));
    }
}
