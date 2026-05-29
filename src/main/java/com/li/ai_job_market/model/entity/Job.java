package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位实体，记录招聘职位信息。
 */
@Data
@TableName("job")
public class Job implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long companyId;
    private Long recruiterId;
    private String title;
    private String category;
    private String experienceLevel;
    private String educationLevel;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer salaryMonths;
    private String city;
    private String district;
    private String address;
    private String jobType;
    private Integer headCount;
    private String description;
    private String requirement;
    private String welfare;
    private String tags;
    private String skillsRequired;
    private String status;
    private Integer viewCount;
    private Integer applyCount;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
