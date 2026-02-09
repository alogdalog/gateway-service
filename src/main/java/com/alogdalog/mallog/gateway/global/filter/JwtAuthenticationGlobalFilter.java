package com.alogdalog.mallog.gateway.global.filter;

import com.alogdalog.mallog.gateway.global.response.ErrorCode;
import com.alogdalog.mallog.gateway.global.response.ResponseWriter;
import com.alogdalog.mallog.gateway.global.util.JwtProperties;
import com.alogdalog.mallog.gateway.global.util.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final ResponseWriter responseWriter;

    @Override
    public int getOrder() {
        return -50; // TraceId(-100) 다음
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 1) Skip paths
        if (isSkipPath(path)) {
            log.debug("[JWT] Skip path: {}", path);
            return chain.filter(exchange);
        }

        // 2) Authorization 헤더 확인
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!isValidBearerAuthorizationHeader(authHeader)) {
            log.warn("[JWT] Missing/Invalid Authorization header. path={}", path);
            return responseWriter.writeError(exchange, ErrorCode.UNAUTHORIZED);
        }

        // 3) Bearer 토큰 추출
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (isEmptyToken(token)) {
            log.warn("[JWT] Empty token. path={}", path);
            return responseWriter.writeError(exchange, ErrorCode.UNAUTHORIZED);
        }

        try {
            // 4) 토큰 검증 + Claims 파싱
            var claims = jwtTokenProvider.parseClaims(token);

            // 5) userId(Long) 추출
            Long userId = jwtTokenProvider.extractUserId(claims);

            // 6) downstream 서비스로 전달할 헤더 주입
            String userIdHeaderName = jwtProperties.getUserIdHeaderName();
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(req -> req.headers(headers ->
                            headers.set(userIdHeaderName, String.valueOf(userId))
                    ))
                    .build();

            log.debug("[JWT] Authorized. userId={} path={}", userId, path);
            return chain.filter(mutatedExchange);

        } catch (ExpiredJwtException e) {
            log.warn("[JWT] Token expired. path={}", path);
            return responseWriter.writeError(exchange, ErrorCode.EXPIRED_TOKEN);

        } catch (JwtException | IllegalArgumentException e) {
            // JwtException: 서명/형식/지원여부 등
            // IllegalArgumentException: userId claim 없음/타입 불일치 등
            log.warn("[JWT] Invalid token. path={} reason={}", path, e.getMessage());
            return responseWriter.writeError(exchange, ErrorCode.INVALID_TOKEN);
        }
    }

    private boolean isSkipPath(String path) {
        return jwtProperties.getSkipPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private boolean isValidBearerAuthorizationHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    private boolean isEmptyToken(String token) {
        return token.isEmpty();
    }
}
