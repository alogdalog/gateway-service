package com.alogdalog.mallog.gateway.global.filter;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Configuration
@ConfigurationProperties(prefix = "gateway.jwt")
public class JwtFilterProperties {

    private List<String> skipPaths;
}
