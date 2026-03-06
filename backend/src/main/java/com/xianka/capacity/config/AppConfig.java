package com.xianka.capacity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppConfig(
        String llmBaseUrl,
        String llmApiKey
) {
}
