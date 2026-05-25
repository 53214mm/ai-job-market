package com.li.ai_job_market.model.vo;

import com.li.ai_job_market.model.entity.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResumeVO implements Serializable {
    private Long id;
    private Long userId;
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
    private Boolean isDefault;
    private Integer aiScore;
    private String aiSuggestion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ResumeEducation> educations;
    private List<ResumeWorkExperience> workExperiences;
    private List<ResumeProject> projects;
    private List<ResumeSkill> skills;
    private List<ResumeCertificate> certificates;

    private static final long serialVersionUID = 1L;
}
