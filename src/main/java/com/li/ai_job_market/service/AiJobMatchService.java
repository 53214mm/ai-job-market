package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.AiJobMatch;

import java.util.List;

/** AI职位匹配服务接口，提供求职者与职位的智能匹配计算和匹配结果查询功能 */
public interface AiJobMatchService extends IService<AiJobMatch> {

    void computeMatch(Long seekerId, Long resumeId, Long jobId);

    List<AiJobMatch> listBySeeker(Long seekerId);

    List<AiJobMatch> listByJob(Long jobId);

    Page<AiJobMatch> pageBySeeker(Long seekerId, int current, int size);
}
