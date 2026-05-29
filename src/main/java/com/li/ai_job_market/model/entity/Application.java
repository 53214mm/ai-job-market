package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 投递申请实体，记录求职者对职位的投递信息。
 */
@Data
@TableName("application")
public class Application implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long jobId;
    private Long resumeId;
    private Long seekerId;
    private Long recruiterId;
    private Long companyId;
    private String status;
    private String coverLetter;
    private Integer aiMatchScore;
    private String aiMatchReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
