package com.li.ai_job_market.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求DTO，封装用户注册时提交的账户信息。
 */
@Data
public class RegisterRequest implements Serializable {
    private String email;
    private String password;
    private String checkPassword;
    private String role;
    private String nickname;
    private static final long serialVersionUID = 1L;
}
