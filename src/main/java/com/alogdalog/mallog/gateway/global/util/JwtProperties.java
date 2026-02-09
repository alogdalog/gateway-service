package com.alogdalog.mallog.gateway.global.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway.jwt")
public class JwtProperties {
    private String secret;
    private List<String> skipPaths = new ArrayList<>();
    private String userIdClaimKey = "userId";
    private String userIdHeaderName = "X-User-Id";
}

