package com.li.ai_job_market.model.dto.resume;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 简历子项请求DTO，封装简历中的教育、工作、项目、技能、证书等子项通用请求。
 */
@Data
public class SubItemRequest implements Serializable {
    // 通用
    private Long id;
    private Integer sortOrder;

    // 教育
    private String schoolName;
    private String degree;
    private String major;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    // 工作
    private String companyName;
    private String position;
    private String industry;
    private String achievement;
    private String skillsUsed;

    // 项目
    private String projectName;
    private String role;
    private String technologies;
    private String projectUrl;

    // 技能
    private String skillName;
    private String proficiency;
    private Integer monthsOfUse;

    // 证书
    private String certName;
    private String issuingOrg;
    private LocalDate obtainDate;

    private static final long serialVersionUID = 1L;
}
