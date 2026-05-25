package com.li.ai_job_market.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterRequest implements Serializable {
    private String email;
    private String password;
    private String checkPassword;
    private String role;
    private String nickname;
    private static final long serialVersionUID = 1L;
}
