package com.lazypay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @NotBlank(message = "Name is required")
    @Field("name")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Field("email")
    private String email;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be greater than 0")
    @Field("creditLimit")
    private BigDecimal creditLimit;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Dues cannot be negative")
    @Field("dues")
    private BigDecimal dues;
}
