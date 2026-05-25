package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.dto.job.*;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.JobVO;

import java.util.List;

public interface JobService extends IService<Job> {

    long createJob(Long userId, JobCreateRequest req);
    JobVO getJobDetail(Long jobId);
    boolean updateJob(Long jobId, Long userId, JobUpdateRequest req);
    boolean closeJob(Long jobId, Long userId);
    boolean publishJob(Long jobId, Long userId);
    Page<JobVO> listJobs(JobQueryRequest req);
    Page<JobVO> listMyJobs(Long userId, JobQueryRequest req);

    JobSkillTag addSkillTag(Long jobId, Long userId, String skillName, boolean isRequired);
    boolean deleteSkillTag(Long jobId, Long sid, Long userId);

    List<JobVO> recommendJobs(Long seekerId, Long resumeId);

    JobVO toJobVO(Job job);
    void checkJobAccess(Long jobId, Long userId);
}
