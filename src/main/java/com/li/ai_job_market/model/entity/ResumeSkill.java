package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简历技能实体，记录简历中的技能信息。
 */
@Data
@TableName("resume_skill")
public class ResumeSkill implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long resumeId;
    private String skillName;
    private String proficiency;
    private Integer monthsOfUse;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
