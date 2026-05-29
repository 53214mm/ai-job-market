package com.li.ai_job_market.model.dto.application;

import lombok.Data;

import java.io.Serializable;

/**
 * 收藏请求DTO，封装添加收藏时的目标类型和ID。
 */
@Data
public class FavoriteRequest implements Serializable {
    private String targetType;
    private Long targetId;
}
