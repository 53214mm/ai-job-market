package com.li.ai_job_market.model.dto.company;

import com.li.ai_job_market.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyQueryRequest extends PageRequest implements Serializable {
    private String keyword;
    private String industry;
    private String scale;
    private static final long serialVersionUID = 1L;
}
