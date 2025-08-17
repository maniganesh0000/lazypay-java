package com.lazypay.exception;

public class CreditLimitExceededException extends RuntimeException {
    
    public CreditLimitExceededException(String message) {
        super(message);
    }
    
    public CreditLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
