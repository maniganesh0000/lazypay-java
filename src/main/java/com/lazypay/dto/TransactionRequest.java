package com.lazypay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    @NotBlank(message = "User name is required")
    private String userName;
    
    @NotBlank(message = "Merchant name is required")
    private String merchantName;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
    private BigDecimal amount;
}
