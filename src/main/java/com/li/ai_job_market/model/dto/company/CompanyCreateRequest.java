package com.li.ai_job_market.model.dto.company;

import lombok.Data;

import java.io.Serializable;

/**
 * 公司创建请求DTO，封装创建公司时提交的企业信息。
 */
@Data
public class CompanyCreateRequest implements Serializable {
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
