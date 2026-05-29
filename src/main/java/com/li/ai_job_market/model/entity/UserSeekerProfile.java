package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 求职者档案实体，记录求职者的个人详细信息。
 */
@Data
@TableName("user_seeker_profile")
public class UserSeekerProfile implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String realName;
    private String gender;
    private LocalDate birthDate;
    private String educationLevel;
    private String currentCity;
    private String expectedCity;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private String jobStatus;
    private String personalSummary;
    private Integer resumeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
