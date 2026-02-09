package com.alogdalog.mallog.gateway.global.util;

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

    private final SecretKey secretKey;

    public JwtTokenProviderImpl(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (ExpiredJwtException e) {
            Date exp = e.getClaims().getExpiration();
            Date iat = e.getClaims().getIssuedAt();

            long nowEpoch = Instant.now().getEpochSecond();
            long expEpoch = exp != null ? exp.toInstant().getEpochSecond() : -1;
            long diffSec = expEpoch != -1 ? (nowEpoch - expEpoch) : -1;

            log.warn("🚫 [UNAUTHORIZED] JWT EXPIRED: exp={}, iat={}, now={}, expiredAgo={}s",
                    exp, iat, Date.from(Instant.now()), diffSec);

            return false;

        } catch (SecurityException | SignatureException e) {
            // Wrong Security Key Used
            log.warn("🚫 [UNAUTHORIZED] JWT INVALID SIGNATURE: {}", e.getMessage());
            return false;

        } catch (MalformedJwtException e) {
            log.warn("🚫 [UNAUTHORIZED] Wrong JWT Format: {}", e.getMessage());
            return false;

        } catch (UnsupportedJwtException e) {
            log.warn("🚫 [UNAUTHORIZED] UNSUPPORTED JWT : {}", e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            log.warn("🚫 [UNAUTHORIZED] JWT is null or empty : {}", e.getMessage());
            return false;

        } catch (JwtException e) {
            log.warn("🚫 [UNAUTHORIZED] JWT INVALID: {}", e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("🚫 [UNAUTHORIZED] JWT VALIDATION ERROR: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("sub", Long.class);
    }
}
