package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

@Data
public class InterviewRequest implements Serializable {
    private String interviewType;
    private String scheduledTime;
    private Integer durationMinutes;
    private String location;
    private String interviewer;
    private String contactPhone;
    private String feedback;
    private static final long serialVersionUID = 1L;
}
