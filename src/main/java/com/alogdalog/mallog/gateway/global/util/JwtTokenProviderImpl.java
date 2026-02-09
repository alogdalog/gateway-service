package com.alogdalog.mallog.gateway.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProviderImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("gateway.jwt.secret is null or blank");
        }

        // HS256 기준: 충분한 길이의 secret 필요 (짧으면 Keys.hmacShaKeyFor에서 예외)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Claims parseClaims(String token) throws JwtException {
        // 서명 검증 + 만료 검증 + 형식 검증을 수행하며, 실패 시 JwtException 계열 예외 발생
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public Long extractUserId(Claims claims) {
        String key = jwtProperties.getUserIdClaimKey(); // "userId"
        Object raw = claims.get(key);

        if (raw == null) {
            throw new IllegalArgumentException("JWT claim '" + key + "' is missing");
        }

        // JSON 숫자는 Integer/Long 등 Number로 들어올 수 있음
        if (raw instanceof Number number) {
            return number.longValue();
        }

        // 혹시 문자열로 들어오는 경우까지 방어
        if (raw instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("JWT claim '" + key + "' is not a number: " + s);
            }
        }

        throw new IllegalArgumentException(
                "JWT claim '" + key + "' has unsupported type: " + raw.getClass().getName()
        );
    }

}
