package com.lazypay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOnboardRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be greater than 0")
    private BigDecimal creditLimit;
}
