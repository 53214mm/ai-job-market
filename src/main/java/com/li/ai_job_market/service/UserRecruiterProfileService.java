package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.UserRecruiterProfile;

/** 招聘方用户档案服务接口，提供招聘方档案查询和公司关联功能 */
public interface UserRecruiterProfileService extends IService<UserRecruiterProfile> {

    UserRecruiterProfile getByUserId(Long userId);

    Long getCompanyIdByUserId(Long userId);
}
