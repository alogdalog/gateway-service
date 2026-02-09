package com.alogdalog.mallog.gateway.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {

    private final boolean success;
    private final String code;
    private final String message;

    private final String path;
    private final String traceId;

    public static ApiErrorResponse from(ErrorCode errorCode, String path, String traceId) {
        return ApiErrorResponse.builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .traceId(traceId)
                .build();
    }
}
