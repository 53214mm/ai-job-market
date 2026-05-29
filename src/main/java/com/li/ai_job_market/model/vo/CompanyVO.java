package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司视图对象，展示企业详细信息，包含职位数量、平均评分及评价列表。
 */
@Data
public class CompanyVO implements Serializable {
    private Long id;
    private String name;
    private String shortName;
    private String logoUrl;
    private String industry;
    private String scale;
    private String stage;
    private String website;
    private String address;
    private String description;
    private String culture;
    private String welfare;
    private Boolean verified;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer jobCount;
    private Double avgRating;
    private List<CompanyReviewVO> reviews;
    private static final long serialVersionUID = 1L;
}
