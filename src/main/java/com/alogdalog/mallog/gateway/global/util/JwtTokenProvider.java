package com.alogdalog.mallog.gateway.global.util;

public interface JwtTokenProvider {

    boolean validateToken(String token);
    Long getUserId(String token);
}
