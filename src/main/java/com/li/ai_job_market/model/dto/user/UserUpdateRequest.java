package com.li.ai_job_market.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private static final long serialVersionUID = 1L;
}
