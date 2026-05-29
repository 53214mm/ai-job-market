package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试实体，记录面试安排的时间、类型和反馈。
 */
@Data
@TableName("interview")
public class Interview implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;
    private String interviewType;
    private LocalDateTime scheduledTime;
    private Integer durationMinutes;
    private String location;
    private String interviewer;
    private String contactPhone;
    private String status;
    private String feedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
