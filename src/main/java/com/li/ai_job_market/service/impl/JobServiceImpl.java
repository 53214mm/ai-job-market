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
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Resource
    @Qualifier("jobAppVectorStore")
    private VectorStore vectorStore;

    @Resource
    private org.springframework.ai.embedding.EmbeddingModel embeddingModel;

    @Resource
    @Qualifier("pgJdbcTemplate")
    private JdbcTemplate pgJdbcTemplate;

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
        boolean ok = this.updateById(job);
        // 异步向量化（不阻塞发布流程）
        try { vectorizeJob(jobId); } catch (Exception e) { log.warn("职位向量化失败: {}", e.getMessage()); }
        return ok;
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

    // ==================== AI 语义搜索 ====================

    @Override
    public Page<JobVO> semanticSearch(String query, JobQueryRequest filters) {
        if (StringUtils.isBlank(query)) return listJobs(filters);

        Map<Long, Double> scoreMap = new LinkedHashMap<>();
        try {
            // 1. EmbeddingModel 向量化查询
            float[] qEmb = embeddingModel.embed(query);
            String vecStr = java.util.Arrays.toString(qEmb);

            // 2. JDBC 直连 pgvector 做余弦相似度检索
            JdbcTemplate jt = pgJdbcTemplate;
            String sql = "SELECT id, metadata, 1 - (embedding <=> ?::vector) AS sim " +
                         "FROM job_vectors " +
                         "ORDER BY embedding <=> ?::vector LIMIT ?";
            List<Map<String, Object>> rows = jt.queryForList(sql, vecStr, vecStr, 50);
            log.info("语义搜索: query='{}', JDBC 匹配 {} 条", query, rows.size());

            // 3. 解析 metadata（PGobject 转为 String）
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            for (Map<String, Object> row : rows) {
                try {
                    Object metaRaw = row.get("metadata");
                    String metaJson = metaRaw != null ? metaRaw.toString() : "{}";
                    @SuppressWarnings("unchecked")
                    Map<String, Object> meta = om.readValue(metaJson, Map.class);
                    Object jid = meta.get("jobId");
                    if (jid == null) continue;
                    Long jobId = jid instanceof Number n ? n.longValue() : Long.valueOf(jid.toString());
                    double sim = ((Number) row.get("sim")).doubleValue();
                    scoreMap.putIfAbsent(jobId, sim);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.error("语义搜索失败: {}", e.getMessage());
            return new Page<>(filters.getCurrent(), filters.getPageSize(), 0);
        }
        if (scoreMap.isEmpty()) return new Page<>(filters.getCurrent(), filters.getPageSize(), 0);

        // 从 MySQL 查询完整 Job 数据
        LambdaQueryWrapper<Job> w = new LambdaQueryWrapper<>();
        w.in(Job::getId, scoreMap.keySet());
        w.eq(Job::getStatus, "PUBLISHED");
        // 叠加精确筛选
        if (StringUtils.isNotBlank(filters.getCity())) w.eq(Job::getCity, filters.getCity());
        if (StringUtils.isNotBlank(filters.getCategory())) w.eq(Job::getCategory, filters.getCategory());
        if (filters.getSalaryMin() != null) w.ge(Job::getSalaryMax, filters.getSalaryMin());
        w.orderByDesc(Job::getPublishedAt);

        List<Job> jobs = this.list(w);
        // 按相似度排序
        jobs.sort((a, b) -> Double.compare(
                scoreMap.getOrDefault(b.getId(), 0.0),
                scoreMap.getOrDefault(a.getId(), 0.0)));

        // 手动分页
        int total = jobs.size();
        int from = (filters.getCurrent() - 1) * filters.getPageSize();
        int to = Math.min(from + filters.getPageSize(), total);
        List<JobVO> voList = jobs.subList(Math.min(from, total), to).stream()
                .map(j -> {
                    JobVO vo = buildJobVO(j);
                    vo.setMatchScore((int) (scoreMap.getOrDefault(j.getId(), 0.0) * 100));
                    return vo;
                }).toList();

        Page<JobVO> page = new Page<>(filters.getCurrent(), filters.getPageSize(), total);
        page.setRecords(voList);
        return page;
    }

    @Override
    public void vectorizeJob(Long jobId) {
        Job job = this.getById(jobId);
        if (job == null || !"PUBLISHED".equals(job.getStatus())) return;

        // 拼接职位文本用于向量化
        String text = String.join(" | ",
                job.getTitle() != null ? job.getTitle() : "",
                job.getDescription() != null ? job.getDescription() : "",
                job.getSkillsRequired() != null ? job.getSkillsRequired() : "",
                job.getTags() != null ? job.getTags() : "",
                job.getCity() != null ? job.getCity() : "",
                job.getCategory() != null ? job.getCategory() : ""
        ).replaceAll("\\s+", " ").trim();

        if (text.length() < 10) return;

        // 用 JDBC 直写 pgvector（绕过 PgVectorStore metadata 兼容问题）
        try {
            float[] emb = embeddingModel.embed(text);
            String vecStr = java.util.Arrays.toString(emb);
            JdbcTemplate jt = pgJdbcTemplate;
            jt.update("DELETE FROM job_vectors WHERE metadata->>'jobId' = ?", String.valueOf(jobId));
            jt.update("INSERT INTO job_vectors (id, content, metadata, embedding) " +
                      "VALUES (?::uuid, ?, ?::jsonb, ?::vector)",
                      java.util.UUID.randomUUID().toString(), text,
                      "{\"jobId\":" + jobId + "}", vecStr);
            log.info("职位向量化完成: jobId={}", jobId);
        } catch (Exception e) {
            log.error("职位向量化失败: jobId={}, error={}", jobId, e.getMessage());
        }
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
