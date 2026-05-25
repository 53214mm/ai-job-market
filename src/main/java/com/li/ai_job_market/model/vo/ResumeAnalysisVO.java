package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ResumeAnalysisVO implements Serializable {
    private Long id;
    private Long resumeId;
    private Integer overallScore;
    private Integer formatScore;
    private Integer contentScore;
    private Integer keywordScore;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String optimizedContent;
    private LocalDateTime createdAt;
    private static final long serialVersionUID = 1L;
}
