package com.alogdalog.mallog.gateway.global.filter;


import com.alogdalog.mallog.gateway.global.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;;
import org.springframework.http.HttpStatus;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // check Access Header
        String authHeader = getAuthHeader(exchange);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }


    private String getAuthHeader(final ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("Authorization");
    }

    private String getAccessToken(String authHeader) {
        return authHeader.substring(7);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
