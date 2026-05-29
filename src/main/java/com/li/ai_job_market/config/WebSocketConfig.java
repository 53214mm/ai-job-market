package com.li.ai_job_market.config;

import com.li.ai_job_market.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 配置类，配置 STOMP 消息代理及 JWT 握手拦截
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;

    public WebSocketConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅前缀：/user 用于点对点，/topic 用于广播
        registry.enableSimpleBroker("/user", "/topic");
        // 客户端发送消息前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点消息默认前缀 /user，转换为 /user/{userId}/...
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        JwtHandshakeInterceptor interceptor = new JwtHandshakeInterceptor();
        // 原生 WebSocket（@stomp/stompjs brokerURL 模式）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:8123")
                .addInterceptors(interceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        Object uid = attributes.get("userId");
                        return uid != null ? new StompPrincipal((Long) uid) : null;
                    }
                });
        // SockJS 降级（sockjs-client）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:8123")
                .addInterceptors(interceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    org.springframework.messaging.MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && accessor.getUser() == null) {
                    String token = accessor.getFirstNativeHeader("token");
                    if (token != null) {
                        Long userId = jwtUtils.getUserId(token);
                        if (userId != null) {
                            accessor.setUser(new StompPrincipal(userId));
                        }
                    }
                }
                return message;
            }
        });
    }

    /**
     * WebSocket 握手时从 query param 提取 JWT token，存入 attributes 供 CONNECT 帧使用。
     * 注意：SockJS 不支持握手时设置 Principal，但 token 可通过 attributes 传递。
     */
    private class JwtHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                String token = servletRequest.getServletRequest().getParameter("token");
                if (token != null) {
                    Long userId = jwtUtils.getUserId(token);
                    if (userId != null) {
                        attributes.put("userId", userId);
                        return true;
                    }
                }
            }
            return true; // 允许匿名连接，token 在 CONNECT 帧 header 中传递
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }

    /** 简易 Principal 实现 */
    public record StompPrincipal(Long userId) implements Principal {
        @Override
        public String getName() {
            return String.valueOf(userId);
        }
    }
}
