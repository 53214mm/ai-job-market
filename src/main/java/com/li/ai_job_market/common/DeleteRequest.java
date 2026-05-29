package com.li.ai_job_market.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求通用 DTO，封装删除操作的 id 参数
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
