package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("company_review")
public class CompanyReview implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long companyId;
    private Long userId;
    private Integer rating;
    private String title;
    private String content;
    private String pros;
    private String cons;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
