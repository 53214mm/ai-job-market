package com.li.ai_job_market.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginRequest implements Serializable {
    private String email;
    private String password;
    private static final long serialVersionUID = 1L;
}
