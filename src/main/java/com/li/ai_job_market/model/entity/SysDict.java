package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class SysDict implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String dictType;
    private String dictKey;
    private String dictValue;
    private Integer sortOrder;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
