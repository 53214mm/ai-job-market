package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long categoryId;
    private Long authorId;
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    private String tags;
    private Integer viewCount;
    private String status;
    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
