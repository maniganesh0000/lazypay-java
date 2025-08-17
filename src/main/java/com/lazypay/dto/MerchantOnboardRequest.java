package com.lazypay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOnboardRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee percentage cannot be negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Fee percentage cannot exceed 100%")
    private BigDecimal feePercentage;
}
