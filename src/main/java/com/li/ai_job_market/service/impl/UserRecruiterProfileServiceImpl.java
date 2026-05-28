package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.mapper.UserRecruiterProfileMapper;
import com.li.ai_job_market.model.entity.UserRecruiterProfile;
import com.li.ai_job_market.service.UserRecruiterProfileService;
import org.springframework.stereotype.Service;

@Service
public class UserRecruiterProfileServiceImpl extends ServiceImpl<UserRecruiterProfileMapper, UserRecruiterProfile>
        implements UserRecruiterProfileService {

    @Override
    public UserRecruiterProfile getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<UserRecruiterProfile>()
                .eq(UserRecruiterProfile::getUserId, userId));
    }

    @Override
    public Long getCompanyIdByUserId(Long userId) {
        UserRecruiterProfile profile = getByUserId(userId);
        return profile != null ? profile.getCompanyId() : null;
    }
}
