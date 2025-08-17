package com.lazypay.service;

import com.lazypay.domain.User;
import com.lazypay.dto.UserOnboardRequest;
import com.lazypay.exception.UserNotFoundException;
import com.lazypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Mono<User> onboardUser(UserOnboardRequest request) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCreditLimit(request.getCreditLimit());
        user.setDues(BigDecimal.ZERO);
        user.setApiKey(generateApiKey());
        
        return userRepository.save(user);
    }

    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)));
    }

    public Mono<User> getUserByName(String name) {
        return userRepository.findByName(name)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with name: " + name)));
    }

    public Mono<User> updateUserDues(String userName, BigDecimal newDues) {
        return getUserByName(userName)
                .flatMap(user -> {
                    user.setDues(newDues);
                    return userRepository.save(user);
                });
    }

    public Mono<Boolean> checkCreditLimit(String userName, BigDecimal amount) {
        return getUserByName(userName)
                .map(user -> {
                    BigDecimal availableCredit = user.getCreditLimit().subtract(user.getDues());
                    return availableCredit.compareTo(amount) >= 0;
                });
    }
    
    private String generateApiKey() {
        return "USER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
