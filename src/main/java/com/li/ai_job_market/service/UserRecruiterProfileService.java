package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.UserRecruiterProfile;

public interface UserRecruiterProfileService extends IService<UserRecruiterProfile> {

    UserRecruiterProfile getByUserId(Long userId);

    Long getCompanyIdByUserId(Long userId);
}
