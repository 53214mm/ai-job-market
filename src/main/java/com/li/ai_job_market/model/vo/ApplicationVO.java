package com.li.ai_job_market.model.vo;

import com.li.ai_job_market.model.entity.ApplicationLog;
import com.li.ai_job_market.model.entity.Interview;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请视图对象，展示职位申请的完整信息，包含关联的职位、公司、日志和面试信息。
 */
@Data
public class ApplicationVO implements Serializable {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String jobTitle;
    private String companyName;
    private String seekerName;
    private String resumeTitle;

    private List<ApplicationLog> logs;
    private Interview interview;
    private static final long serialVersionUID = 1L;
}
