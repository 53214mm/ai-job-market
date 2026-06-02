package com.li.ai_job_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.li.ai_job_market.model.entity.Application;

public interface ApplicationMapper extends BaseMapper<Application> {

    /** 检查招聘方是否有投递记录引用了该简历（用于权限校验） */
    @org.apache.ibatis.annotations.Select(
        "SELECT COUNT(*) FROM application a INNER JOIN job j ON a.job_id = j.id " +
        "WHERE a.resume_id = #{resumeId} AND j.recruiter_id = #{recruiterId}")
    int countByResumeIdAndRecruiterId(
        @org.apache.ibatis.annotations.Param("resumeId") Long resumeId,
        @org.apache.ibatis.annotations.Param("recruiterId") Long recruiterId);
}
