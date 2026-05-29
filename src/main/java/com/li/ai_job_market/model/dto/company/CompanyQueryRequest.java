package com.li.ai_job_market.model.dto.company;

import com.li.ai_job_market.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 公司查询请求DTO，封装公司列表的分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyQueryRequest extends PageRequest implements Serializable {
    private String keyword;
    private String industry;
    private String scale;
    private static final long serialVersionUID = 1L;
}
