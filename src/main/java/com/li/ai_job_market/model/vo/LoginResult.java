package com.li.ai_job_market.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录结果VO，封装登录成功后的JWT令牌和用户信息。
 */
@Data
@AllArgsConstructor
public class LoginResult {
    private String token;
    private UserVO user;
}
