package com.alogdalog.mallog.gateway.global.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public int getOrder() {
        return -100; // 가장 먼저
    }

    /**
     * 모든 요청에 대해 호출되는 GlobalFilter 메서드
     *
     * @param exchange 요청/응답 정보를 담고 있는 컨텍스트 객체
     * @param chain 다음 필터 또는 실제 라우팅을 수행하는 체인
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 요청 단위로 고유 trace id 생성
        String traceId = UUID.randomUUID().toString();

        // 기존 요청(exchange)를 변경해서 새로운 요청 객체로 만들기 (webflux는 불변객체라 직접 수정 못해서 새로 만들어야함)
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder ->
                        // 요청 헤더에 tracd id 추가
                        builder.header(TRACE_ID_HEADER, traceId)
                ).build();

        // 일단 응다베 헤더에 넣어서 디버깅하기 위함
        mutatedExchange.getResponse().beforeCommit(() -> {
            mutatedExchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);
            return Mono.empty();
        });

        // gateway-service에서 로그 남기기
        log.info("[Gateway] traceId={} | method={} | path={}",
                traceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath()
                );

        // 다음 필터 또는 라우팅 수행하기
        return chain.filter(mutatedExchange);
    }
}
