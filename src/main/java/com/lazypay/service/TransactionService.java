package com.lazypay.service;

import com.lazypay.domain.Transaction;
import com.lazypay.dto.TransactionRequest;
import com.lazypay.exception.CreditLimitExceededException;
import com.lazypay.exception.MerchantNotFoundException;
import com.lazypay.exception.UserNotFoundException;
import com.lazypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private TransactionRepository transactionRepository;

    public Mono<Transaction> createTransaction(TransactionRequest request) {
        return Mono.zip(
                userService.getUserByName(request.getUserName()),
                merchantService.getMerchantByName(request.getMerchantName())
        ).flatMap(tuple -> {
            var user = tuple.getT1();
            var merchant = tuple.getT2();
            
            // Check credit limit
            BigDecimal availableCredit = user.getCreditLimit().subtract(user.getDues());
            if (availableCredit.compareTo(request.getAmount()) < 0) {
                return Mono.error(new CreditLimitExceededException(
                    "Credit limit exceeded. Available: " + availableCredit + ", Required: " + request.getAmount()
                ));
            }
            
            // Calculate fee
            BigDecimal feeAmount = request.getAmount()
                    .multiply(merchant.getFeePercentage())
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            
            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setUserName(request.getUserName());
            transaction.setMerchantName(request.getMerchantName());
            transaction.setAmount(request.getAmount());
            transaction.setStatus("APPROVED");
            transaction.setFeeAmount(feeAmount);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());
            
            // Update user dues
            BigDecimal newDues = user.getDues().add(request.getAmount());
            
            // Update merchant total fee collected
            return Mono.zip(
                    userService.updateUserDues(request.getUserName(), newDues),
                    merchantService.updateTotalFeeCollected(request.getMerchantName(), feeAmount),
                    transactionRepository.save(transaction)
            ).map(tuple2 -> tuple2.getT3());
        });
    }

    public Mono<Transaction> getTransactionById(String id) {
        return transactionRepository.findById(id);
    }
}
