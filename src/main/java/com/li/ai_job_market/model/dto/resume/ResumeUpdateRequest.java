package com.li.ai_job_market.model.dto.resume;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResumeUpdateRequest implements Serializable {
    private Long id;
    private String title;
    private String fullName;
    private String email;
    private String phone;
    private String currentCity;
    private String expectedCity;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private String jobStatus;
    private String summary;
    private String privacy;
    private static final long serialVersionUID = 1L;
}
