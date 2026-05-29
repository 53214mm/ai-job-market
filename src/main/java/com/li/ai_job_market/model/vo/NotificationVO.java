package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知视图对象，展示系统通知的详情及类型的中文标签。
 */
@Data
public class NotificationVO implements Serializable {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createdAt;
    private String typeLabel;
    private static final long serialVersionUID = 1L;
}
