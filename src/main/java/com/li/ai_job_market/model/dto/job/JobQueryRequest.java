package com.li.ai_job_market.model.dto.job;

import com.li.ai_job_market.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 职位查询请求DTO，封装职位搜索的分页条件及排序方式。
 */
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
