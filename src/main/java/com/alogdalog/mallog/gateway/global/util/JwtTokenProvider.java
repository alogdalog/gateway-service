package com.alogdalog.mallog.gateway.global.util;

import io.jsonwebtoken.Claims;

public interface JwtTokenProvider {
    Claims parseClaims(String token);
    Long extractUserId(Claims claims);
}
