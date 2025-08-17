package com.lazypay.service;

import com.lazypay.domain.Merchant;
import com.lazypay.dto.MerchantOnboardRequest;
import com.lazypay.exception.MerchantNotFoundException;
import com.lazypay.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    public Mono<Merchant> onboardMerchant(MerchantOnboardRequest request) {
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID().toString());
        merchant.setName(request.getName());
        merchant.setFeePercentage(request.getFeePercentage());
        merchant.setTotalFeeCollected(BigDecimal.ZERO);
        merchant.setApiKey(generateApiKey());
        
        return merchantRepository.save(merchant);
    }

    public Mono<Merchant> getMerchantByName(String name) {
        return merchantRepository.findByName(name)
                .switchIfEmpty(Mono.error(new MerchantNotFoundException("Merchant not found with name: " + name)));
    }

    public Mono<Merchant> updateFeePercentage(String merchantName, BigDecimal newFeePercentage) {
        return getMerchantByName(merchantName)
                .flatMap(merchant -> {
                    merchant.setFeePercentage(newFeePercentage);
                    return merchantRepository.save(merchant);
                });
    }

    public Mono<Merchant> updateTotalFeeCollected(String merchantName, BigDecimal feeAmount) {
        return getMerchantByName(merchantName)
                .flatMap(merchant -> {
                    BigDecimal newTotal = merchant.getTotalFeeCollected().add(feeAmount);
                    merchant.setTotalFeeCollected(newTotal);
                    return merchantRepository.save(merchant);
                });
    }

    public Mono<BigDecimal> getTotalFeeCollected(String merchantName) {
        return getMerchantByName(merchantName)
                .map(Merchant::getTotalFeeCollected);
    }
    
    private String generateApiKey() {
        return "MERCHANT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
