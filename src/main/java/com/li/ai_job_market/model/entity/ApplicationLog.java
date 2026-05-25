package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("application_log")
public class ApplicationLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
