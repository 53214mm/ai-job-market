package com.li.ai_job_market.model.dto.company;

import lombok.Data;

import java.io.Serializable;

/**
 * 公司更新请求DTO，封装更新公司信息时提交的字段。
 */
@Data
public class CompanyUpdateRequest implements Serializable {
    private Long id;
    private String name;
    private String shortName;
    private String industry;
    private String scale;
    private String stage;
    private String website;
    private String address;
    private String description;
    private String culture;
    private String welfare;
    private static final long serialVersionUID = 1L;
}
