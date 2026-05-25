package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApplicationRequest implements Serializable {
    private Long jobId;
    private Long resumeId;
    private String coverLetter;
    private static final long serialVersionUID = 1L;
}
