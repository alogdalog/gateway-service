package com.alogdalog.mallog.gateway.global.filter;


import com.alogdalog.mallog.gateway.global.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;


// GlobalFilter Component: automatically connect to security filter

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtFilterProperties jwtFilterProperties;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // path distinguish: skip filter
        String path = getPath(exchange);
        log.info("➡️ [GatewayFilterChain] route path: {}", path);
        if (isSkipPath(path)) {
            log.info("️🧭 [ROUTE] Skip Filter");
            return chain.filter(exchange);
        }

        // check Access Header
        String authHeader = getAuthHeader(exchange);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            log.info("🚫 [UNAUTHORIZED] No Bearer On JWT Access Token");
            return unauthorized(exchange);
        }

        // extract access token from header(Authorization)
        String accessToken = getAccessToken(authHeader);
        if (!jwtTokenProvider.validateToken(accessToken)) {
            return unauthorized(exchange);
        }

        // put userId into HTTP Request Message Header as 'X-User-Id'
        Long userId = jwtTokenProvider.getUserId(accessToken);
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.add("X-User-Id", String.valueOf(userId));
                }))
                .build();

        log.info("🔐 [ROUTE] Authorization Success");
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private String getPath(final ServerWebExchange exchange) {
        return exchange.getRequest().getURI().getPath();
    }

    private boolean isSkipPath(String path) {
        return jwtFilterProperties.getSkipPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private String getAuthHeader(final ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);
    }

    private String getAccessToken(String authHeader) {
        return authHeader.substring(BEARER.length());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
