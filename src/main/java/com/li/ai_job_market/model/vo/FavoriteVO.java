package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FavoriteVO implements Serializable {
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;

    private String targetTitle;
    private String targetCompany;
    private String targetCity;
    private Integer targetSalaryMax;
    private static final long serialVersionUID = 1L;
}
