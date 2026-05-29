package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

/**
 * 申请状态变更请求DTO，封装变更申请状态时的目标状态和备注。
 */
@Data
public class ApplicationStatusRequest implements Serializable {
    private String status;
    private String remark;
}
