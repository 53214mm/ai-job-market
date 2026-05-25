package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("company")
public class Company implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
