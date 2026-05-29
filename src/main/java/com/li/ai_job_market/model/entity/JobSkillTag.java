package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位技能标签实体，记录职位要求的技能。
 */
@Data
@TableName("job_skill_tag")
public class JobSkillTag implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long jobId;
    private String skillName;
    private Boolean isRequired;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
