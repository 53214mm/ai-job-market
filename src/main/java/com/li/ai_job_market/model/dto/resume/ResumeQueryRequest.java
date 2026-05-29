package com.li.ai_job_market.model.dto.resume;

import com.li.ai_job_market.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 简历查询请求DTO，封装简历列表的分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResumeQueryRequest extends PageRequest implements Serializable {
    private Long userId;
    private String title;
    private String privacy;
    private static final long serialVersionUID = 1L;
}
