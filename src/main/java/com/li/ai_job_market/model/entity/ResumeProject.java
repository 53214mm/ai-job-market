package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历项目经历实体，记录简历中的项目经验。
 */
@Data
@TableName("resume_project")
public class ResumeProject implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long resumeId;
    private String projectName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String technologies;
    private String achievement;
    private String projectUrl;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
