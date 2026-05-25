package com.li.ai_job_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ai_interview_record")
public class AiInterviewRecord implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long jobId;
    private Long sessionId;
    private String question;
    private String expectedAnswerKeywords;
    private String userAnswer;
    private String aiFeedback;
    private Integer score;
    private Integer questionIndex;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
