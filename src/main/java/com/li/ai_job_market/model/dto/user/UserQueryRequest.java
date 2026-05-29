package com.li.ai_job_market.model.dto.user;

import com.li.ai_job_market.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求DTO，封装用户列表的分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {
    private Long id;
    private String email;
    private String phone;
    private String role;
    private String nickname;
    private String status;
    private static final long serialVersionUID = 1L;
}
