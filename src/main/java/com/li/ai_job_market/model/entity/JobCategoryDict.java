package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位分类字典实体，记录职位分类的层级树结构。
 */
@Data
@TableName("job_category_dict")
public class JobCategoryDict implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
