package com.alogdalog.mallog.gateway.global.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseWriter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final ObjectMapper objectMapper;

    public Mono<Void> writeError(ServerWebExchange exchange, ErrorCode errorCode) {
        String path = exchange.getRequest().getURI().getPath();
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);

        ApiErrorResponse body = ApiErrorResponse.from(errorCode, path, traceId);

        exchange.getResponse().setStatusCode(errorCode.getHttpStatus());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패 시 최소한의 응답만이라도 내려주기
            log.error("[Gateway] Failed to serialize ApiErrorResponse: {}", e.getMessage(), e);
            String fallback = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal error\"}";
            bytes = fallback.getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
