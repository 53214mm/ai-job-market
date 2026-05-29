package com.li.ai_job_market.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求DTO，封装用户可修改的个人信息字段。
 */
@Data
public class UserUpdateRequest implements Serializable {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private static final long serialVersionUID = 1L;
}
