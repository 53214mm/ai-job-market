package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简历实体，记录求职者的简历基本信息。
 */
@Data
@TableName("resume")
public class Resume implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String title;
    private String fullName;
    private String email;
    private String phone;
    private String currentCity;
    private String expectedCity;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private String jobStatus;
    private String summary;
    private String privacy;
    private Boolean isDefault;
    private Integer aiScore;
    private String aiSuggestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
