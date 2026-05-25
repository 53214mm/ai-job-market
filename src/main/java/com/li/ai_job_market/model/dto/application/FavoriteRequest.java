package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

@Data
public class FavoriteRequest implements Serializable {
    private String targetType;
    private Long targetId;
}
