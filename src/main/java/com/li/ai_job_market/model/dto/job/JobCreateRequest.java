package com.li.ai_job_market.model.dto.job;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class JobCreateRequest implements Serializable {
    private String title;
    private String category;
    private String experienceLevel;
    private String educationLevel;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer salaryMonths;
    private String city;
    private String district;
    private String address;
    private String jobType;
    private Integer headCount;
    private String description;
    private String requirement;
    private String welfare;
    private String tags;
    private String skillsRequired;
    private List<SkillTagItem> skillTags;

    @Data
    public static class SkillTagItem implements Serializable {
        private String skillName;
        private Boolean isRequired;
        private Integer sortOrder;
    }
    private static final long serialVersionUID = 1L;
}
