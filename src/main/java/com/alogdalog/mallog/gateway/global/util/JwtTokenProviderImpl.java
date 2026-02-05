package com.alogdalog.mallog.gateway.global.util;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {
    @Override
    public boolean validateToken(String token) {
        return false;
    }

    @Override
    public Long getUserId(String token) {
        return 0L;
    }
}
