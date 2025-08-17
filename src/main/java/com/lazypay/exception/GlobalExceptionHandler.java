package com.lazypay.exception;

import com.lazypay.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CreditLimitExceededException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleCreditLimitExceeded(CreditLimitExceededException ex) {
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleUserNotFound(UserNotFoundException ex) {
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleMerchantNotFound(MerchantNotFoundException ex) {
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleValidationException(WebExchangeBindException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        
        ApiResponse<String> response = ApiResponse.error(errorMessage);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleGenericException(Exception ex) {
        ApiResponse<String> response = ApiResponse.error("Internal server error: " + ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}
