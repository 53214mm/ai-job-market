package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.mapper.UserSeekerProfileMapper;
import com.li.ai_job_market.model.entity.UserSeekerProfile;
import com.li.ai_job_market.service.UserSeekerProfileService;
import org.springframework.stereotype.Service;

/** 求职方用户档案服务实现，提供求职者档案的基础数据访问功能 */
@Service
public class UserSeekerProfileServiceImpl
        extends ServiceImpl<UserSeekerProfileMapper, UserSeekerProfile>
        implements UserSeekerProfileService {
}
