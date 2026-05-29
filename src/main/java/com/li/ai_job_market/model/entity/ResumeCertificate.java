package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历证书实体，记录简历中的证书信息。
 */
@Data
@TableName("resume_certificate")
public class ResumeCertificate implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long resumeId;
    private String certName;
    private String issuingOrg;
    private LocalDate obtainDate;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
