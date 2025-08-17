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
@Document(collection = "paybacks")
public class Payback {
    
    @Id
    private String id;
    
    @NotBlank(message = "User name is required")
    @Field("userName")
    private String userName;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be at least 0.01")
    @Field("amount")
    private BigDecimal amount;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Remaining dues cannot be negative")
    @Field("remainingDues")
    private BigDecimal remainingDues;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
}
