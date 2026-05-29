package com.li.ai_job_market.model.vo;

import com.li.ai_job_market.model.entity.JobSkillTag;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位视图对象，展示职位详细信息，包含公司信息、技能标签和匹配分数。
 */
@Data
public class JobVO implements Serializable {
    private Long id;
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private Boolean companyVerified;
    private String title;
    private String category;
    private String experienceLevel;
    private String educationLevel;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer salaryMonths;
    private String city;
    private String district;
    private String jobType;
    private Integer headCount;
    private String description;
    private String requirement;
    private String welfare;
    private String tags;
    private String skillsRequired;
    private String status;
    private Integer viewCount;
    private Integer applyCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<JobSkillTag> skillTags;
    private Integer matchScore;
    private static final long serialVersionUID = 1L;
}
