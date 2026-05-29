package com.li.ai_job_market.model.dto.company;

import lombok.Data;

import java.io.Serializable;

/**
 * 公司评价请求DTO，封装用户提交的企业评价内容。
 */
@Data
public class CompanyReviewRequest implements Serializable {
    private Integer rating;
    private String title;
    private String content;
    private String pros;
    private String cons;
    private static final long serialVersionUID = 1L;
}
