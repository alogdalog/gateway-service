package com.alogdalog.mallog.gateway.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Gateway: CSRF OFF
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Disable Default login
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // Authority check in GlobalFilter
                // route auth request direct to auth service
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/auth/**"
                        ).permitAll()
                        .anyExchange().permitAll()
                )
                .build();
    }

}
