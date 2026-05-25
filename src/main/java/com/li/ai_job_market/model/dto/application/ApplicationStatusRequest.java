package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApplicationStatusRequest implements Serializable {
    private String status;
    private String remark;
}
