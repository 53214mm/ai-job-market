package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MessageVO implements Serializable {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long applicationId;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
    private String senderName;
    private String receiverName;
    private static final long serialVersionUID = 1L;
}
