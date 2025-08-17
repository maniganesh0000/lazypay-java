package com.lazypay.service;

import com.lazypay.domain.User;
import com.lazypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Service
public class ReportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantService merchantService;

    public Mono<BigDecimal> getTotalFeeCollected(String merchantName) {
        return merchantService.getTotalFeeCollected(merchantName);
    }

    public Mono<BigDecimal> getDuesForUser(String userName) {
        return userRepository.findByName(userName)
                .map(User::getDues);
    }

    public Flux<User> getUsersAtCreditLimit() {
        return userRepository.findAll()
                .filter(user -> user.getDues().compareTo(user.getCreditLimit()) >= 0);
    }

    public Mono<Map<String, Object>> getTotalDuesBreakdown() {
        return userRepository.findAll()
                .collectList()
                .map(users -> {
                    Map<String, Object> breakdown = new HashMap<>();
                    
                    BigDecimal totalDues = users.stream()
                            .map(User::getDues)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal totalCreditLimit = users.stream()
                            .map(User::getCreditLimit)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal totalAvailableCredit = totalCreditLimit.subtract(totalDues);
                    
                    breakdown.put("totalDues", totalDues);
                    breakdown.put("totalCreditLimit", totalCreditLimit);
                    breakdown.put("totalAvailableCredit", totalAvailableCredit);
                    breakdown.put("userCount", users.size());
                    breakdown.put("usersAtCreditLimit", users.stream()
                            .filter(user -> user.getDues().compareTo(user.getCreditLimit()) >= 0)
                            .count());
                    
                    return breakdown;
                });
    }
}
