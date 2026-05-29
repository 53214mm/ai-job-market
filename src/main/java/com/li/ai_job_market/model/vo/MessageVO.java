package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息视图对象，展示私信消息的详细信息及收发双方昵称。
 */
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
