package com.lazypay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private String status;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>("success", message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<T>("success", message, null);
    }
    
    public static <T> ApiResponse<T> error(String reason) {
        return new ApiResponse<T>("error", reason, null);
    }
}
