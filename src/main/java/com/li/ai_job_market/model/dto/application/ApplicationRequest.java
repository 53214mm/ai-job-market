package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

/**
 * 投递申请请求DTO，封装投递职位时的职位、简历及求职信信息。
 */
@Data
public class ApplicationRequest implements Serializable {
    private Long jobId;
    private Long resumeId;
    private String coverLetter;
    private static final long serialVersionUID = 1L;
}
