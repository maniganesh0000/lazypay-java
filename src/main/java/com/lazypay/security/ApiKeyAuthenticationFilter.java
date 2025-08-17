package com.lazypay.security;

import com.lazypay.domain.User;
import com.lazypay.domain.Merchant;
import com.lazypay.repository.UserRepository;
import com.lazypay.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class ApiKeyAuthenticationFilter implements WebFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    private final ServerSecurityContextRepository securityContextRepository = new WebSessionServerSecurityContextRepository();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return unauthorizedResponse(exchange, "Missing API key");
        }

        return authenticateApiKey(apiKey)
                .flatMap(authentication -> {
                    SecurityContext securityContext = new SecurityContextImpl(authentication);
                    return securityContextRepository.save(exchange, securityContext)
                            .then(chain.filter(exchange));
                })
                .onErrorResume(e -> unauthorizedResponse(exchange, "Invalid API key"));
    }

    private Mono<Authentication> authenticateApiKey(String apiKey) {
        return Mono.zip(
                userRepository.findByApiKey(apiKey),
                merchantRepository.findByApiKey(apiKey)
        ).flatMap(tuple -> {
            User user = tuple.getT1();
            Merchant merchant = tuple.getT2();
            
            if (user != null) {
                return Mono.just(new UsernamePasswordAuthenticationToken(
                    user.getName(), null, Collections.emptyList()));
            } else if (merchant != null) {
                return Mono.just(new UsernamePasswordAuthenticationToken(
                    merchant.getName(), null, Collections.emptyList()));
            } else {
                return Mono.error(new RuntimeException("Invalid API key"));
            }
        }).switchIfEmpty(Mono.error(new RuntimeException("Invalid API key")));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String responseBody = String.format(
            "{\"status\":\"error\",\"reason\":\"%s\"}", reason);
        
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory()
                .wrap(responseBody.getBytes(StandardCharsets.UTF_8))));
    }
}
