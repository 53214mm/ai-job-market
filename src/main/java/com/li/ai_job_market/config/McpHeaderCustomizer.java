package com.li.ai_job_market.config;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * MCP HTTP 请求鉴权定制器 —— Spring AI 1.1.2 原生支持
 * <p>
 * 实现 McpSyncHttpClientRequestCustomizer，在每次 MCP HTTP 请求前
 * 向 HttpRequest.Builder 注入 Authorization 头。
 */
@Configuration
public class McpHeaderCustomizer {

    private static final Logger log = LoggerFactory.getLogger(McpHeaderCustomizer.class);

    @Value("${mxai.api-key}")
    private String apiKey;

    @Bean
    public McpSyncHttpClientRequestCustomizer mcpAuthCustomizer() {
        log.info("====== MCP 鉴权定制器已注册 ======");
        return (HttpRequest.Builder requestBuilder,
                String serverName,
                URI uri,
                String method,
                McpTransportContext context) -> {
            requestBuilder.header("Authorization", apiKey);
        };
    }
}
