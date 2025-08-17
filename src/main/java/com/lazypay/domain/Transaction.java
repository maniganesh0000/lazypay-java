package com.lazypay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    
    @Id
    private String id;
    
    @NotBlank(message = "User name is required")
    @Field("userName")
    private String userName;
    
    @NotBlank(message = "Merchant name is required")
    @Field("merchantName")
    private String merchantName;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
    @Field("amount")
    private BigDecimal amount;
    
    @NotBlank(message = "Status is required")
    @Field("status")
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    
    @Field("reason")
    private String reason; // Optional reason for rejection or other status
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee amount cannot be negative")
    @Field("feeAmount")
    private BigDecimal feeAmount;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
