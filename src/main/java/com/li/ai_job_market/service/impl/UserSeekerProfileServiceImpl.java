package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.mapper.UserSeekerProfileMapper;
import com.li.ai_job_market.model.entity.UserSeekerProfile;
import com.li.ai_job_market.service.UserSeekerProfileService;
import org.springframework.stereotype.Service;

@Service
public class UserSeekerProfileServiceImpl
        extends ServiceImpl<UserSeekerProfileMapper, UserSeekerProfile>
        implements UserSeekerProfileService {
}
