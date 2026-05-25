package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.*;
import com.li.ai_job_market.model.dto.application.InterviewRequest;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.ApplicationVO;
import com.li.ai_job_market.model.vo.FavoriteVO;
import com.li.ai_job_market.service.ApplicationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements ApplicationService {

    @Resource private JobMapper jobMapper;
    @Resource private ResumeMapper resumeMapper;
    @Resource private CompanyMapper companyMapper;
    @Resource private ApplicationLogMapper logMapper;
    @Resource private InterviewMapper interviewMapper;
    @Resource private FavoriteMapper favoriteMapper;
    @Resource private ResumeSkillMapper resumeSkillMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long apply(Long seekerId, Long jobId, Long resumeId, String coverLetter) {
        Job job = jobMapper.selectById(jobId);
        ThrowUtils.throwIf(job == null || !"PUBLISHED".equals(job.getStatus()),
                ErrorCode.NOT_FOUND_ERROR, "职位不可投递");

        Resume resume = resumeMapper.selectById(resumeId);
        ThrowUtils.throwIf(resume == null || !resume.getUserId().equals(seekerId),
                ErrorCode.NO_AUTH_ERROR, "无权使用此简历");

        // 计算 AI 匹配度
        int matchScore = calcMatchScore(resumeId, job);

        Application app = new Application();
        app.setJobId(jobId);
        app.setResumeId(resumeId);
        app.setSeekerId(seekerId);
        app.setRecruiterId(job.getRecruiterId());
        app.setCompanyId(job.getCompanyId());
        app.setStatus("APPLIED");
        app.setCoverLetter(coverLetter);
        app.setAiMatchScore(matchScore);
        this.save(app);

        // job apply_count +1
        Job updateJob = new Job();
        updateJob.setId(jobId);
        updateJob.setApplyCount(job.getApplyCount() + 1);
        jobMapper.updateById(updateJob);

        // 日志
        writeLog(app.getId(), null, "APPLIED", seekerId, null);
        log.info("投递: appId={}, seekerId={}, jobId={}", app.getId(), seekerId, jobId);
        return app.getId();
    }

    @Override
    public ApplicationVO getDetail(Long id, Long userId) {
        Application app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "投递不存在");
        return buildVO(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Long operatorId, String status, String remark) {
        Application app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "投递不存在");

        String oldStatus = app.getStatus();
        app.setStatus(status);
        this.updateById(app);
        writeLog(id, oldStatus, status, operatorId, remark);
        return true;
    }

    @Override
    public Page<ApplicationVO> listMyApplications(Long seekerId, int current, int size) {
        LambdaQueryWrapper<Application> w = new LambdaQueryWrapper<>();
        w.eq(Application::getSeekerId, seekerId).orderByDesc(Application::getCreatedAt);
        Page<Application> page = this.page(new Page<>(current, size), w);
        Page<ApplicationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::buildVO).toList());
        return voPage;
    }

    @Override
    public Page<ApplicationVO> listReceivedApplications(Long recruiterId, int current, int size, String status) {
        LambdaQueryWrapper<Application> w = new LambdaQueryWrapper<>();
        w.eq(Application::getRecruiterId, recruiterId);
        if (StringUtils.isNotBlank(status)) w.eq(Application::getStatus, status);
        w.orderByDesc(Application::getCreatedAt);
        Page<Application> page = this.page(new Page<>(current, size), w);
        Page<ApplicationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::buildVO).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Interview scheduleInterview(Long applicationId, Long recruiterId, InterviewRequest req) {
        Application app = this.getById(applicationId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "投递不存在");

        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setInterviewType(req.getInterviewType());
        if (StringUtils.isNotBlank(req.getScheduledTime()))
            interview.setScheduledTime(LocalDateTime.parse(req.getScheduledTime().replace(" ", "T")));
        interview.setDurationMinutes(req.getDurationMinutes());
        interview.setLocation(req.getLocation());
        interview.setInterviewer(req.getInterviewer());
        interview.setContactPhone(req.getContactPhone());
        interview.setStatus("SCHEDULED");
        interviewMapper.insert(interview);

        updateStatus(applicationId, recruiterId, "INTERVIEW", "安排面试: " + req.getInterviewType());
        return interview;
    }

    @Override
    public boolean updateInterviewFeedback(Long iid, Long recruiterId, String feedback) {
        Interview interview = interviewMapper.selectById(iid);
        ThrowUtils.throwIf(interview == null, ErrorCode.NOT_FOUND_ERROR, "面试不存在");
        interview.setFeedback(feedback);
        interview.setStatus("COMPLETED");
        return interviewMapper.updateById(interview) > 0;
    }

    // ==================== 收藏 ====================

    @Override
    public long addFavorite(Long userId, String targetType, Long targetId) {
        // 检查重复
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, userId).eq(Favorite::getTargetType, targetType).eq(Favorite::getTargetId, targetId);
        Favorite exist = favoriteMapper.selectOne(w);
        if (exist != null) return exist.getId();

        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(targetType);
        f.setTargetId(targetId);
        favoriteMapper.insert(f);
        return f.getId();
    }

    @Override
    public boolean removeFavorite(Long id, Long userId) {
        Favorite f = favoriteMapper.selectById(id);
        ThrowUtils.throwIf(f == null || !f.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作");
        return favoriteMapper.deleteById(id) > 0;
    }

    @Override
    public List<FavoriteVO> listFavorites(Long userId) {
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreatedAt);
        return favoriteMapper.selectList(w).stream().map(f -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(f.getId()); vo.setUserId(f.getUserId());
            vo.setTargetType(f.getTargetType()); vo.setTargetId(f.getTargetId());
            vo.setCreatedAt(f.getCreatedAt());
            if ("JOB".equals(f.getTargetType())) {
                Job job = jobMapper.selectById(f.getTargetId());
                if (job != null) {
                    vo.setTargetTitle(job.getTitle());
                    vo.setTargetCity(job.getCity());
                    vo.setTargetSalaryMax(job.getSalaryMax());
                    Company c = companyMapper.selectById(job.getCompanyId());
                    if (c != null) vo.setTargetCompany(c.getName());
                }
            }
            return vo;
        }).toList();
    }

    // ==================== 辅助方法 ====================

    private int calcMatchScore(Long resumeId, Job job) {
        LambdaQueryWrapper<ResumeSkill> w = new LambdaQueryWrapper<>();
        w.eq(ResumeSkill::getResumeId, resumeId);
        List<ResumeSkill> skills = resumeSkillMapper.selectList(w);
        if (skills.isEmpty() || StringUtils.isBlank(job.getSkillsRequired())) return 50;
        Set<String> s = new HashSet<>();
        for (ResumeSkill sk : skills) s.add(sk.getSkillName().toLowerCase());
        int match = 0, total = 0;
        for (String req : job.getSkillsRequired().split("[,，]")) {
            String r = req.trim().toLowerCase();
            if (!r.isEmpty()) { total++; if (s.contains(r)) match++; }
        }
        return total == 0 ? 50 : match * 100 / total;
    }

    private void writeLog(Long appId, String from, String to, Long operator, String remark) {
        ApplicationLog lg = new ApplicationLog();
        lg.setApplicationId(appId);
        lg.setFromStatus(from);
        lg.setToStatus(to);
        lg.setOperatorId(operator);
        lg.setRemark(remark);
        logMapper.insert(lg);
    }

    private ApplicationVO buildVO(Application app) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(app.getId()); vo.setJobId(app.getJobId()); vo.setResumeId(app.getResumeId());
        vo.setSeekerId(app.getSeekerId()); vo.setRecruiterId(app.getRecruiterId());
        vo.setCompanyId(app.getCompanyId()); vo.setStatus(app.getStatus());
        vo.setCoverLetter(app.getCoverLetter()); vo.setAiMatchScore(app.getAiMatchScore());
        vo.setAiMatchReason(app.getAiMatchReason());
        vo.setCreatedAt(app.getCreatedAt()); vo.setUpdatedAt(app.getUpdatedAt());

        Job job = jobMapper.selectById(app.getJobId());
        if (job != null) vo.setJobTitle(job.getTitle());
        Company c = companyMapper.selectById(app.getCompanyId());
        if (c != null) vo.setCompanyName(c.getName());
        Resume r = resumeMapper.selectById(app.getResumeId());
        if (r != null) vo.setResumeTitle(r.getTitle());

        // 日志
        LambdaQueryWrapper<ApplicationLog> lw = new LambdaQueryWrapper<>();
        lw.eq(ApplicationLog::getApplicationId, app.getId()).orderByDesc(ApplicationLog::getCreatedAt);
        vo.setLogs(logMapper.selectList(lw));

        // 面试
        LambdaQueryWrapper<Interview> iw = new LambdaQueryWrapper<>();
        iw.eq(Interview::getApplicationId, app.getId()).orderByDesc(Interview::getCreatedAt).last("LIMIT 1");
        vo.setInterview(interviewMapper.selectOne(iw));

        return vo;
    }
}
