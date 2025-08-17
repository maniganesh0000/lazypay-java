package com.lazypay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "merchants")
public class Merchant {
    
    @Id
    private String id;
    
    @NotBlank(message = "Name is required")
    @Field("name")
    private String name;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee percentage cannot be negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Fee percentage cannot exceed 100%")
    @Field("feePercentage")
    private BigDecimal feePercentage;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Total fee collected cannot be negative")
    @Field("totalFeeCollected")
    private BigDecimal totalFeeCollected;
}
