package com.lazypay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class PayLaterServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PayLaterServiceApplication.class, args);
    }
}
