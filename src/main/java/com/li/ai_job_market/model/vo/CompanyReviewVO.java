package com.li.ai_job_market.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CompanyReviewVO implements Serializable {
    private Long id;
    private Long companyId;
    private Long userId;
    private Integer rating;
    private String title;
    private String content;
    private String pros;
    private String cons;
    private String status;
    private String userNickname;
    private LocalDateTime createdAt;
    private static final long serialVersionUID = 1L;
}
