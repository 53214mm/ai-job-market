package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.BusinessException;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.mapper.AiJobMatchMapper;
import com.li.ai_job_market.mapper.JobMapper;
import com.li.ai_job_market.mapper.ResumeMapper;
import com.li.ai_job_market.mapper.ResumeSkillMapper;
import com.li.ai_job_market.model.entity.AiJobMatch;
import com.li.ai_job_market.model.entity.Job;
import com.li.ai_job_market.model.entity.Resume;
import com.li.ai_job_market.model.entity.ResumeSkill;
import com.li.ai_job_market.service.AiJobMatchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AiJobMatchServiceImpl extends ServiceImpl<AiJobMatchMapper, AiJobMatch>
        implements AiJobMatchService {

    @Resource private ResumeMapper resumeMapper;
    @Resource private ResumeSkillMapper resumeSkillMapper;
    @Resource private JobMapper jobMapper;

    @Override
    public void computeMatch(Long seekerId, Long resumeId, Long jobId) {
        Resume resume = resumeMapper.selectById(resumeId);
        Job job = jobMapper.selectById(jobId);
        if (resume == null || job == null) return;
        if (!resume.getUserId().equals(seekerId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此简历");
        }

        LambdaQueryWrapper<ResumeSkill> w = new LambdaQueryWrapper<>();
        w.eq(ResumeSkill::getResumeId, resumeId);
        List<ResumeSkill> skills = resumeSkillMapper.selectList(w);

        Set<String> skillNames = new HashSet<>();
        for (ResumeSkill s : skills) skillNames.add(s.getSkillName().toLowerCase());

        int match = 0, total = 0;
        if (StringUtils.isNotBlank(job.getSkillsRequired())) {
            for (String req : job.getSkillsRequired().split("[,，]")) {
                String r = req.trim().toLowerCase();
                if (!r.isEmpty()) { total++; if (skillNames.contains(r)) match++; }
            }
        }

        int score = total == 0 ? 50 : match * 100 / total;

        AiJobMatch entity = new AiJobMatch();
        entity.setSeekerId(seekerId);
        entity.setResumeId(resumeId);
        entity.setJobId(jobId);
        entity.setMatchScore(BigDecimal.valueOf(score));
        entity.setDimensionScores("{\"skill\":" + score + "}");
        entity.setMatchReason("技能匹配度 " + score + "%");
        this.save(entity);
    }

    @Override
    public List<AiJobMatch> listBySeeker(Long seekerId) {
        return this.list(new LambdaQueryWrapper<AiJobMatch>()
                .eq(AiJobMatch::getSeekerId, seekerId)
                .orderByDesc(AiJobMatch::getMatchScore));
    }

    @Override
    public List<AiJobMatch> listByJob(Long jobId) {
        return this.list(new LambdaQueryWrapper<AiJobMatch>()
                .eq(AiJobMatch::getJobId, jobId)
                .orderByDesc(AiJobMatch::getMatchScore));
    }

    @Override
    public Page<AiJobMatch> pageBySeeker(Long seekerId, int current, int size) {
        LambdaQueryWrapper<AiJobMatch> w = new LambdaQueryWrapper<>();
        w.eq(AiJobMatch::getSeekerId, seekerId).orderByDesc(AiJobMatch::getMatchScore);
        return this.page(new Page<>(current, size), w);
    }
}
