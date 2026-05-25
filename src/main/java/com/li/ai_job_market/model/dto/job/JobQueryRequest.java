package com.li.ai_job_market.model.dto.job;

import com.li.ai_job_market.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobQueryRequest extends PageRequest implements Serializable {
    private String keyword;
    private String city;
    private String category;
    private String experienceLevel;
    private String jobType;
    private Integer salaryMin;
    private Integer salaryMax;
    private String sortBy;
    private static final long serialVersionUID = 1L;
}
