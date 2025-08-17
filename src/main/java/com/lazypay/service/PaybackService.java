package com.lazypay.service;

import com.lazypay.domain.Payback;
import com.lazypay.dto.PaybackRequest;
import com.lazypay.repository.PaybackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaybackService {

    @Autowired
    private UserService userService;

    @Autowired
    private PaybackRepository paybackRepository;

    public Mono<Payback> recordPayback(PaybackRequest request) {
        return userService.getUserByName(request.getUserName())
                .flatMap(user -> {
                    // Calculate new dues
                    BigDecimal newDues = user.getDues().subtract(request.getAmount());
                    if (newDues.compareTo(BigDecimal.ZERO) < 0) {
                        newDues = BigDecimal.ZERO;
                    }
                    
                    // Update user dues
                    return userService.updateUserDues(request.getUserName(), newDues)
                            .flatMap(updatedUser -> {
                                // Create payback record
                                Payback payback = new Payback();
                                payback.setId(UUID.randomUUID().toString());
                                payback.setUserName(request.getUserName());
                                payback.setAmount(request.getAmount());
                                payback.setRemainingDues(newDues);
                                payback.setCreatedAt(LocalDateTime.now());
                                
                                return paybackRepository.save(payback);
                            });
                });
    }

    public Mono<Payback> getPaybackById(String id) {
        return paybackRepository.findById(id);
    }
}
