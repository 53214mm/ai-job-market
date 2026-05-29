package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI职位匹配实体，记录简历与职位的智能匹配结果。
 */
@Data
@TableName("ai_job_match")
public class AiJobMatch implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long seekerId;
    private Long resumeId;
    private Long jobId;
    private BigDecimal matchScore;
    private String dimensionScores;
    private String matchReason;
    private BigDecimal vectorSimilarity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
