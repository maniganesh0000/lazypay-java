package com.lazypay.config;

import com.lazypay.security.ApiKeyAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .authorizeExchange(authz -> authz
                .pathMatchers("/test/health").permitAll() // Health check endpoint
                .anyExchange().authenticated()
            )
            .addFilterAt(apiKeyAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        
        return http.build();
    }
}
