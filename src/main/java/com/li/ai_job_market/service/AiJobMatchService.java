package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.AiJobMatch;

import java.util.List;

public interface AiJobMatchService extends IService<AiJobMatch> {

    void computeMatch(Long seekerId, Long resumeId, Long jobId);

    List<AiJobMatch> listBySeeker(Long seekerId);

    List<AiJobMatch> listByJob(Long jobId);

    Page<AiJobMatch> pageBySeeker(Long seekerId, int current, int size);
}
