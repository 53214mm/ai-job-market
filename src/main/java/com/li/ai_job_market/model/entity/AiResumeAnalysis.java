package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI简历分析实体，记录简历的AI分析评分和建议。
 */
@Data
@TableName("ai_resume_analysis")
public class AiResumeAnalysis implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long resumeId;
    private Long seekerId;
    private Integer overallScore;
    private Integer formatScore;
    private Integer contentScore;
    private Integer keywordScore;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String optimizedContent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
