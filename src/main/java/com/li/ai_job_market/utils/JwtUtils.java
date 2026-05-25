package com.li.ai_job_market.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具类 - Token 生成与验证
 */
@Slf4j
@Component
public class JwtUtils {

    // TODO: 生产环境改为复杂密钥并通过 @Value 注入
    private static final String SECRET = "ai-job-market-secret-key-2026-must-be-long-enough";
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7天

    /**
     * 生成 Token
     * @param userId 用户ID
     * @param role   用户角色
     */
    public String generateToken(Long userId, String role) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 验证 Token 并返回解析结果
     * @return DecodedJWT，验证失败返回 null
     */
    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
        } catch (JWTVerificationException e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取 userId
     */
    public Long getUserId(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt == null) return null;
        return jwt.getClaim("userId").asLong();
    }

    /**
     * 从 Token 中获取 role
     */
    public String getRole(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt == null) return null;
        return jwt.getClaim("role").asString();
    }
}