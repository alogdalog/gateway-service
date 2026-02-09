package com.alogdalog.mallog.gateway.global.config;

import com.alogdalog.mallog.gateway.global.util.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtPropertiesCheckRunner implements CommandLineRunner {
    private final JwtProperties jwtProperties;

    @Override
    public void run(String... args) {
        log.info("[CHECK] gateway.jwt.secret is null? {}", jwtProperties.getSecret() == null);
        log.info("[CHECK] gateway.jwt.skipPaths = {}", jwtProperties.getSkipPaths());
        log.info("[CHECK] gateway.jwt.userIdClaimKey = {}", jwtProperties.getUserIdClaimKey());
        log.info("[CHECK] gateway.jwt.userIdHeaderName = {}", jwtProperties.getUserIdHeaderName());

    }
}
