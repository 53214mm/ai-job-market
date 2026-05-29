package com.li.ai_job_market.model.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送消息请求DTO，封装发送私信时的接收者及消息内容。
 */
@Data
public class SendMessageReq implements Serializable {
    private Long receiverId;
    private Long applicationId;
    private String content;
    private static final long serialVersionUID = 1L;
}
