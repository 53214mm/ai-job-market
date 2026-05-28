package com.li.ai_job_market.model.dto.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SendMessageReq implements Serializable {
    private Long receiverId;
    private Long applicationId;
    private String content;
    private static final long serialVersionUID = 1L;
}
