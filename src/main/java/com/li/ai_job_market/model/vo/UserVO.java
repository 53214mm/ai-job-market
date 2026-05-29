package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图对象，展示用户的基本信息（不含敏感字段如密码）。
 */
@Data
public class UserVO implements Serializable {
    private Long id;
    private String email;
    private String phone;
    private String role;
    private String nickname;
    private String avatarUrl;
    private String status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private static final long serialVersionUID = 1L;
}
