package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.*;
import com.li.ai_job_market.model.dto.job.*;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.JobVO;
import com.li.ai_job_market.service.JobService;
import com.li.ai_job_market.service.UserRecruiterProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/** 职位管理服务实现，提供职位发布、编辑、搜索和基于简历技能的推荐功能 */
@Slf4j
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Resource
    private CompanyMapper companyMapper;
    @Resource
    private UserRecruiterProfileService recruiterProfileService;
    @Resource
    private JobSkillTagMapper skillTagMapper;
    @Resource
    private ResumeMapper resumeMapper;
    @Resource
    private ResumeSkillMapper resumeSkillMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createJob(Long userId, JobCreateRequest req) {
        ThrowUtils.throwIf(StringUtils.isBlank(req.getTitle()),
                ErrorCode.PARAMS_ERROR, "职位名称不能为空");

        UserRecruiterProfile profile = recruiterProfileService.getOne(
                new LambdaQueryWrapper<UserRecruiterProfile>().eq(UserRecruiterProfile::getUserId, userId));
        ThrowUtils.throwIf(profile == null || profile.getCompanyId() == null,
                ErrorCode.NO_AUTH_ERROR, "请先创建公司后再发布职位");
        Company company = companyMapper.selectById(profile.getCompanyId());
        ThrowUtils.throwIf(company == null, ErrorCode.NOT_FOUND_ERROR, "公司不存在");

        Job job = new Job();
        job.setCompanyId(company.getId());
        job.setRecruiterId(userId);
        job.setTitle(req.getTitle());
        job.setCategory(req.getCategory());
        job.setExperienceLevel(req.getExperienceLevel());
        job.setEducationLevel(req.getEducationLevel());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setSalaryMonths(req.getSalaryMonths() != null ? req.getSalaryMonths() : 12);
        job.setCity(req.getCity());
        job.setDistrict(req.getDistrict());
        job.setAddress(req.getAddress());
        job.setJobType(req.getJobType());
        job.setHeadCount(req.getHeadCount() != null ? req.getHeadCount() : 1);
        job.setDescription(req.getDescription());
        job.setRequirement(req.getRequirement());
        job.setWelfare(req.getWelfare());
        job.setTags(req.getTags());
        job.setSkillsRequired(req.getSkillsRequired());
        job.setStatus("DRAFT");
        job.setViewCount(0);
        job.setApplyCount(0);

        boolean saved = this.save(job);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "发布职位失败");

        if (req.getSkillTags() != null) {
            for (JobCreateRequest.SkillTagItem item : req.getSkillTags()) {
                JobSkillTag tag = new JobSkillTag();
                tag.setJobId(job.getId());
                tag.setSkillName(item.getSkillName());
                tag.setIsRequired(item.getIsRequired() != null ? item.getIsRequired() : true);
                tag.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
                skillTagMapper.insert(tag);
            }
        }
        log.info("职位发布: id={}, title={}, companyId={}", job.getId(), req.getTitle(), company.getId());
        return job.getId();
    }

    @Override
    public JobVO getJobDetail(Long jobId) {
        Job job = this.getById(jobId);
        ThrowUtils.throwIf(job == null, ErrorCode.NOT_FOUND_ERROR, "职位不存在");

        Job updateView = new Job();
        updateView.setId(jobId);
        updateView.setViewCount(job.getViewCount() + 1);
        this.updateById(updateView);

        return buildJobVO(job);
    }

    @Override
    public boolean updateJob(Long jobId, Long userId, JobUpdateRequest req) {
        checkJobAccess(jobId, userId);
        Job job = new Job();
        job.setId(jobId);
        if (req.getTitle() != null) job.setTitle(req.getTitle());
        if (req.getCategory() != null) job.setCategory(req.getCategory());
        if (req.getExperienceLevel() != null) job.setExperienceLevel(req.getExperienceLevel());
        if (req.getEducationLevel() != null) job.setEducationLevel(req.getEducationLevel());
        if (req.getSalaryMin() != null) job.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null) job.setSalaryMax(req.getSalaryMax());
        if (req.getSalaryMonths() != null) job.setSalaryMonths(req.getSalaryMonths());
        if (req.getCity() != null) job.setCity(req.getCity());
        if (req.getJobType() != null) job.setJobType(req.getJobType());
        if (req.getHeadCount() != null) job.setHeadCount(req.getHeadCount());
        if (req.getDescription() != null) job.setDescription(req.getDescription());
        if (req.getRequirement() != null) job.setRequirement(req.getRequirement());
        if (req.getTags() != null) job.setTags(req.getTags());
        if (req.getSkillsRequired() != null) job.setSkillsRequired(req.getSkillsRequired());
        return this.updateById(job);
    }

    @Override
    public boolean closeJob(Long jobId, Long userId) {
        checkJobAccess(jobId, userId);
        Job job = new Job();
        job.setId(jobId);
        job.setStatus("CLOSED");
        return this.updateById(job);
    }

    @Override
    public boolean publishJob(Long jobId, Long userId) {
        checkJobAccess(jobId, userId);
        Job job = new Job();
        job.setId(jobId);
        job.setStatus("PUBLISHED");
        job.setPublishedAt(LocalDateTime.now());
        return this.updateById(job);
    }

    @Override
    public Page<JobVO> listJobs(JobQueryRequest req) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getStatus, "PUBLISHED");
        if (StringUtils.isNotBlank(req.getKeyword())) {
            wrapper.and(w -> w.like(Job::getTitle, req.getKeyword())
                             .or().like(Job::getDescription, req.getKeyword()));
        }
        wrapper.eq(StringUtils.isNotBlank(req.getCity()), Job::getCity, req.getCity());
        wrapper.eq(StringUtils.isNotBlank(req.getCategory()), Job::getCategory, req.getCategory());
        wrapper.eq(StringUtils.isNotBlank(req.getExperienceLevel()), Job::getExperienceLevel, req.getExperienceLevel());
        wrapper.eq(StringUtils.isNotBlank(req.getJobType()), Job::getJobType, req.getJobType());
        if (req.getSalaryMin() != null) wrapper.ge(Job::getSalaryMin, req.getSalaryMin());
        if (req.getSalaryMax() != null) wrapper.le(Job::getSalaryMin, req.getSalaryMax());

        if ("salary".equals(req.getSortBy())) {
            wrapper.orderByDesc(Job::getSalaryMax);
        } else {
            wrapper.orderByDesc(Job::getPublishedAt);
        }

        Page<Job> page = this.page(new Page<>(req.getCurrent(), req.getPageSize()), wrapper);
        Page<JobVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::buildJobVO).toList());
        return voPage;
    }

    @Override
    public Page<JobVO> listMyJobs(Long userId, JobQueryRequest req) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getRecruiterId, userId);
        if (StringUtils.isNotBlank(req.getKeyword())) {
            wrapper.like(Job::getTitle, req.getKeyword());
        }
        wrapper.orderByDesc(Job::getCreatedAt);

        Page<Job> page = this.page(new Page<>(req.getCurrent(), req.getPageSize()), wrapper);
        Page<JobVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::buildJobVO).toList());
        return voPage;
    }

    @Override
    public List<JobVO> recommendJobs(Long seekerId, Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) return Collections.emptyList();

        LambdaQueryWrapper<ResumeSkill> skillW = new LambdaQueryWrapper<>();
        skillW.eq(ResumeSkill::getResumeId, resumeId);
        List<ResumeSkill> skills = resumeSkillMapper.selectList(skillW);

        LambdaQueryWrapper<Job> jobW = new LambdaQueryWrapper<>();
        jobW.eq(Job::getStatus, "PUBLISHED").orderByDesc(Job::getPublishedAt).last("LIMIT 50");
        List<Job> jobs = this.list(jobW);

        Set<String> skillNames = new HashSet<>();
        for (ResumeSkill s : skills) skillNames.add(s.getSkillName().toLowerCase());

        List<JobVO> result = jobs.stream().map(j -> {
            JobVO vo = buildJobVO(j);
            int matchCount = 0;
            if (StringUtils.isNotBlank(j.getSkillsRequired())) {
                for (String s : j.getSkillsRequired().split("[,，]")) {
                    if (skillNames.contains(s.trim().toLowerCase())) matchCount++;
                }
            }
            vo.setMatchScore(skillNames.isEmpty() ? 0 : matchCount * 100 / skillNames.size());
            return vo;
        }).sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore())).toList();

        return result.subList(0, Math.min(20, result.size()));
    }

    // ==================== 技能标签 ====================

    @Override
    public JobSkillTag addSkillTag(Long jobId, Long userId, String skillName, boolean isRequired) {
        checkJobAccess(jobId, userId);
        JobSkillTag tag = new JobSkillTag();
        tag.setJobId(jobId);
        tag.setSkillName(skillName);
        tag.setIsRequired(isRequired);
        skillTagMapper.insert(tag);
        return tag;
    }

    @Override
    public boolean deleteSkillTag(Long jobId, Long sid, Long userId) {
        checkJobAccess(jobId, userId);
        return skillTagMapper.deleteById(sid) > 0;
    }

    // ==================== 辅助方法 ====================

    @Override
    public JobVO toJobVO(Job job) {
        JobVO vo = new JobVO();
        vo.setId(job.getId());
        vo.setCompanyId(job.getCompanyId());
        vo.setTitle(job.getTitle());
        vo.setCategory(job.getCategory());
        vo.setExperienceLevel(job.getExperienceLevel());
        vo.setEducationLevel(job.getEducationLevel());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setSalaryMonths(job.getSalaryMonths());
        vo.setCity(job.getCity());
        vo.setDistrict(job.getDistrict());
        vo.setJobType(job.getJobType());
        vo.setHeadCount(job.getHeadCount());
        vo.setDescription(job.getDescription());
        vo.setRequirement(job.getRequirement());
        vo.setWelfare(job.getWelfare());
        vo.setTags(job.getTags());
        vo.setSkillsRequired(job.getSkillsRequired());
        vo.setStatus(job.getStatus());
        vo.setViewCount(job.getViewCount());
        vo.setApplyCount(job.getApplyCount());
        vo.setPublishedAt(job.getPublishedAt());
        vo.setCreatedAt(job.getCreatedAt());
        vo.setUpdatedAt(job.getUpdatedAt());
        return vo;
    }

    @Override
    public void checkJobAccess(Long jobId, Long userId) {
        Job job = this.getById(jobId);
        ThrowUtils.throwIf(job == null, ErrorCode.NOT_FOUND_ERROR, "职位不存在");
        ThrowUtils.throwIf(!job.getRecruiterId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "无权操作此职位");
    }

    private JobVO buildJobVO(Job job) {
        JobVO vo = toJobVO(job);
        if (job.getCompanyId() != null) {
            Company company = companyMapper.selectById(job.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getName());
                vo.setCompanyLogo(company.getLogoUrl());
                vo.setCompanyVerified(company.getVerified());
            }
        }
        vo.setSkillTags(skillTagMapper.selectList(
                new LambdaQueryWrapper<JobSkillTag>()
                        .eq(JobSkillTag::getJobId, job.getId())
                        .orderByAsc(JobSkillTag::getSortOrder)));
        return vo;
    }
}
