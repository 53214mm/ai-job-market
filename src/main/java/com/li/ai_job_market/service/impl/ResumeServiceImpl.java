package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.AI.app.JobApp;
import com.li.ai_job_market.AI.tools.PdfUtil;
import com.li.ai_job_market.constant.FileConstant;
import com.li.ai_job_market.exception.BusinessException;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.*;
import com.li.ai_job_market.model.dto.resume.ResumeCreateRequest;
import com.li.ai_job_market.model.dto.resume.ResumeQueryRequest;
import com.li.ai_job_market.model.dto.resume.ResumeUpdateRequest;
import com.li.ai_job_market.model.dto.resume.SubItemRequest;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.ResumeAnalysisVO;
import com.li.ai_job_market.model.vo.ResumeVO;
import com.li.ai_job_market.service.ResumeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 简历管理服务实现，提供简历全生命周期管理，支持AI分析、AI优化及PDF导入导出 */
@Slf4j
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume>
        implements ResumeService {

    @Resource
    private JobApp jobApp;

    @Resource
    private ResumeEducationMapper educationMapper;

    @Resource
    private ResumeWorkExperienceMapper workMapper;

    @Resource
    private ResumeProjectMapper projectMapper;

    @Resource
    private ResumeSkillMapper skillMapper;

    @Resource
    private ResumeCertificateMapper certMapper;

    @Resource
    private AiResumeAnalysisMapper analysisMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM");

    // ==================== 简历 CRUD ====================

    @Override
    public long createResume(Long userId, ResumeCreateRequest req) {
        ThrowUtils.throwIf(StringUtils.isBlank(req.getTitle()),
                ErrorCode.PARAMS_ERROR, "简历标题不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(req.getFullName()),
                ErrorCode.PARAMS_ERROR, "姓名不能为空");

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(req.getTitle());
        resume.setFullName(req.getFullName());
        resume.setEmail(req.getEmail());
        resume.setPhone(req.getPhone());
        resume.setCurrentCity(req.getCurrentCity());
        resume.setExpectedCity(req.getExpectedCity());
        resume.setExpectedSalaryMin(req.getExpectedSalaryMin());
        resume.setExpectedSalaryMax(req.getExpectedSalaryMax());
        resume.setJobStatus(req.getJobStatus());
        resume.setSummary(req.getSummary());
        resume.setPrivacy(StringUtils.isBlank(req.getPrivacy()) ? "PUBLIC" : req.getPrivacy());
        resume.setIsDefault(false);

        boolean saved = this.save(resume);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "创建简历失败");
        log.info("简历创建: id={}, userId={}", resume.getId(), userId);
        return resume.getId();
    }

    @Override
    public ResumeVO getResumeDetail(Long resumeId, Long currentUserId) {
        Resume resume = this.getById(resumeId);
        ThrowUtils.throwIf(resume == null, ErrorCode.NOT_FOUND_ERROR, "简历不存在");

        if ("PRIVATE".equals(resume.getPrivacy())) {
            checkResumeAccess(resumeId, currentUserId);
        }

        ResumeVO vo = toResumeVO(resume);
        vo.setEducations(listEducations(resumeId));
        vo.setWorkExperiences(listWorkExperiences(resumeId));
        vo.setProjects(listProjects(resumeId));
        vo.setSkills(listSkills(resumeId));
        vo.setCertificates(listCertificates(resumeId));
        return vo;
    }

    @Override
    public boolean updateResume(Long resumeId, Long userId, ResumeUpdateRequest req) {
        checkResumeAccess(resumeId, userId);

        Resume resume = new Resume();
        resume.setId(resumeId);
        if (req.getTitle() != null) resume.setTitle(req.getTitle());
        if (req.getFullName() != null) resume.setFullName(req.getFullName());
        if (req.getEmail() != null) resume.setEmail(req.getEmail());
        if (req.getPhone() != null) resume.setPhone(req.getPhone());
        if (req.getCurrentCity() != null) resume.setCurrentCity(req.getCurrentCity());
        if (req.getExpectedCity() != null) resume.setExpectedCity(req.getExpectedCity());
        if (req.getExpectedSalaryMin() != null) resume.setExpectedSalaryMin(req.getExpectedSalaryMin());
        if (req.getExpectedSalaryMax() != null) resume.setExpectedSalaryMax(req.getExpectedSalaryMax());
        if (req.getJobStatus() != null) resume.setJobStatus(req.getJobStatus());
        if (req.getSummary() != null) resume.setSummary(req.getSummary());
        if (req.getPrivacy() != null) resume.setPrivacy(req.getPrivacy());

        return this.updateById(resume);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResume(Long resumeId, Long userId) {
        checkResumeAccess(resumeId, userId);

        deleteByResumeId("education", resumeId);
        deleteByResumeId("work", resumeId);
        deleteByResumeId("project", resumeId);
        deleteByResumeId("skill", resumeId);
        deleteByResumeId("cert", resumeId);

        return this.removeById(resumeId);
    }

    @Override
    public Page<Resume> listMyResumes(Long userId, ResumeQueryRequest req) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        wrapper.eq(StringUtils.isNotBlank(req.getPrivacy()), Resume::getPrivacy, req.getPrivacy());
        wrapper.like(StringUtils.isNotBlank(req.getTitle()), Resume::getTitle, req.getTitle());
        wrapper.orderByDesc(Resume::getUpdatedAt);
        return this.page(new Page<>(req.getCurrent(), req.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long resumeId, Long userId) {
        checkResumeAccess(resumeId, userId);

        // 取消其他默认
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId).eq(Resume::getIsDefault, true);
        List<Resume> defaults = this.list(wrapper);
        for (Resume r : defaults) {
            r.setIsDefault(false);
            this.updateById(r);
        }

        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setIsDefault(true);
        return this.updateById(resume);
    }

    // ==================== 教育经历 ====================

    @Override
    public ResumeEducation addEducation(Long resumeId, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ThrowUtils.throwIf(StringUtils.isBlank(req.getSchoolName()),
                ErrorCode.PARAMS_ERROR, "学校名称不能为空");

        ResumeEducation edu = new ResumeEducation();
        edu.setResumeId(resumeId);
        edu.setSchoolName(req.getSchoolName());
        edu.setDegree(req.getDegree());
        edu.setMajor(req.getMajor());
        edu.setStartDate(req.getStartDate());
        edu.setEndDate(req.getEndDate());
        edu.setDescription(req.getDescription());
        edu.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        educationMapper.insert(edu);
        return edu;
    }

    @Override
    public ResumeEducation updateEducation(Long resumeId, Long eid, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ResumeEducation edu = educationMapper.selectById(eid);
        ThrowUtils.throwIf(edu == null || !edu.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "教育经历不存在");

        if (req.getSchoolName() != null) edu.setSchoolName(req.getSchoolName());
        if (req.getDegree() != null) edu.setDegree(req.getDegree());
        if (req.getMajor() != null) edu.setMajor(req.getMajor());
        if (req.getStartDate() != null) edu.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) edu.setEndDate(req.getEndDate());
        if (req.getDescription() != null) edu.setDescription(req.getDescription());
        if (req.getSortOrder() != null) edu.setSortOrder(req.getSortOrder());

        educationMapper.updateById(edu);
        return edu;
    }

    @Override
    public boolean deleteEducation(Long resumeId, Long eid, Long userId) {
        checkResumeAccess(resumeId, userId);
        ResumeEducation edu = educationMapper.selectById(eid);
        ThrowUtils.throwIf(edu == null || !edu.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "教育经历不存在");
        return educationMapper.deleteById(eid) > 0;
    }

    // ==================== 工作经历 ====================

    @Override
    public ResumeWorkExperience addWorkExperience(Long resumeId, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ThrowUtils.throwIf(StringUtils.isBlank(req.getCompanyName()),
                ErrorCode.PARAMS_ERROR, "公司名称不能为空");

        ResumeWorkExperience work = new ResumeWorkExperience();
        work.setResumeId(resumeId);
        work.setCompanyName(req.getCompanyName());
        work.setPosition(req.getPosition());
        work.setIndustry(req.getIndustry());
        work.setStartDate(req.getStartDate());
        work.setEndDate(req.getEndDate());
        work.setDescription(req.getDescription());
        work.setAchievement(req.getAchievement());
        work.setSkillsUsed(req.getSkillsUsed());
        work.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        workMapper.insert(work);
        return work;
    }

    @Override
    public ResumeWorkExperience updateWorkExperience(Long resumeId, Long wid, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ResumeWorkExperience work = workMapper.selectById(wid);
        ThrowUtils.throwIf(work == null || !work.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "工作经历不存在");

        if (req.getCompanyName() != null) work.setCompanyName(req.getCompanyName());
        if (req.getPosition() != null) work.setPosition(req.getPosition());
        if (req.getIndustry() != null) work.setIndustry(req.getIndustry());
        if (req.getStartDate() != null) work.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) work.setEndDate(req.getEndDate());
        if (req.getDescription() != null) work.setDescription(req.getDescription());
        if (req.getAchievement() != null) work.setAchievement(req.getAchievement());
        if (req.getSkillsUsed() != null) work.setSkillsUsed(req.getSkillsUsed());
        if (req.getSortOrder() != null) work.setSortOrder(req.getSortOrder());

        workMapper.updateById(work);
        return work;
    }

    @Override
    public boolean deleteWorkExperience(Long resumeId, Long wid, Long userId) {
        checkResumeAccess(resumeId, userId);
        ResumeWorkExperience work = workMapper.selectById(wid);
        ThrowUtils.throwIf(work == null || !work.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "工作经历不存在");
        return workMapper.deleteById(wid) > 0;
    }

    // ==================== 项目经历 ====================

    @Override
    public ResumeProject addProject(Long resumeId, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ThrowUtils.throwIf(StringUtils.isBlank(req.getProjectName()),
                ErrorCode.PARAMS_ERROR, "项目名称不能为空");

        ResumeProject project = new ResumeProject();
        project.setResumeId(resumeId);
        project.setProjectName(req.getProjectName());
        project.setRole(req.getRole());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        project.setDescription(req.getDescription());
        project.setTechnologies(req.getTechnologies());
        project.setAchievement(req.getAchievement());
        project.setProjectUrl(req.getProjectUrl());
        project.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        projectMapper.insert(project);
        return project;
    }

    @Override
    public ResumeProject updateProject(Long resumeId, Long pid, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ResumeProject project = projectMapper.selectById(pid);
        ThrowUtils.throwIf(project == null || !project.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "项目经历不存在");

        if (req.getProjectName() != null) project.setProjectName(req.getProjectName());
        if (req.getRole() != null) project.setRole(req.getRole());
        if (req.getStartDate() != null) project.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) project.setEndDate(req.getEndDate());
        if (req.getDescription() != null) project.setDescription(req.getDescription());
        if (req.getTechnologies() != null) project.setTechnologies(req.getTechnologies());
        if (req.getAchievement() != null) project.setAchievement(req.getAchievement());
        if (req.getProjectUrl() != null) project.setProjectUrl(req.getProjectUrl());
        if (req.getSortOrder() != null) project.setSortOrder(req.getSortOrder());

        projectMapper.updateById(project);
        return project;
    }

    @Override
    public boolean deleteProject(Long resumeId, Long pid, Long userId) {
        checkResumeAccess(resumeId, userId);
        ResumeProject project = projectMapper.selectById(pid);
        ThrowUtils.throwIf(project == null || !project.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "项目经历不存在");
        return projectMapper.deleteById(pid) > 0;
    }

    // ==================== 技能 ====================

    @Override
    public ResumeSkill addSkill(Long resumeId, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ThrowUtils.throwIf(StringUtils.isBlank(req.getSkillName()),
                ErrorCode.PARAMS_ERROR, "技能名称不能为空");

        ResumeSkill skill = new ResumeSkill();
        skill.setResumeId(resumeId);
        skill.setSkillName(req.getSkillName());
        skill.setProficiency(req.getProficiency());
        skill.setMonthsOfUse(req.getMonthsOfUse());
        skill.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        skillMapper.insert(skill);
        return skill;
    }

    @Override
    public ResumeSkill updateSkill(Long resumeId, Long sid, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ResumeSkill skill = skillMapper.selectById(sid);
        ThrowUtils.throwIf(skill == null || !skill.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "技能不存在");

        if (req.getSkillName() != null) skill.setSkillName(req.getSkillName());
        if (req.getProficiency() != null) skill.setProficiency(req.getProficiency());
        if (req.getMonthsOfUse() != null) skill.setMonthsOfUse(req.getMonthsOfUse());
        if (req.getSortOrder() != null) skill.setSortOrder(req.getSortOrder());

        skillMapper.updateById(skill);
        return skill;
    }

    @Override
    public boolean deleteSkill(Long resumeId, Long sid, Long userId) {
        checkResumeAccess(resumeId, userId);
        ResumeSkill skill = skillMapper.selectById(sid);
        ThrowUtils.throwIf(skill == null || !skill.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "技能不存在");
        return skillMapper.deleteById(sid) > 0;
    }

    // ==================== 证书 ====================

    @Override
    public ResumeCertificate addCertificate(Long resumeId, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ThrowUtils.throwIf(StringUtils.isBlank(req.getCertName()),
                ErrorCode.PARAMS_ERROR, "证书名称不能为空");

        ResumeCertificate cert = new ResumeCertificate();
        cert.setResumeId(resumeId);
        cert.setCertName(req.getCertName());
        cert.setIssuingOrg(req.getIssuingOrg());
        cert.setObtainDate(req.getObtainDate());
        cert.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);

        certMapper.insert(cert);
        return cert;
    }

    @Override
    public ResumeCertificate updateCertificate(Long resumeId, Long cid, Long userId, SubItemRequest req) {
        checkResumeAccess(resumeId, userId);
        ResumeCertificate cert = certMapper.selectById(cid);
        ThrowUtils.throwIf(cert == null || !cert.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "证书不存在");

        if (req.getCertName() != null) cert.setCertName(req.getCertName());
        if (req.getIssuingOrg() != null) cert.setIssuingOrg(req.getIssuingOrg());
        if (req.getObtainDate() != null) cert.setObtainDate(req.getObtainDate());
        if (req.getSortOrder() != null) cert.setSortOrder(req.getSortOrder());

        certMapper.updateById(cert);
        return cert;
    }

    @Override
    public boolean deleteCertificate(Long resumeId, Long cid, Long userId) {
        checkResumeAccess(resumeId, userId);
        ResumeCertificate cert = certMapper.selectById(cid);
        ThrowUtils.throwIf(cert == null || !cert.getResumeId().equals(resumeId),
                ErrorCode.NOT_FOUND_ERROR, "证书不存在");
        return certMapper.deleteById(cid) > 0;
    }

    // ==================== AI 分析 ====================

    @Override
    public ResumeAnalysisVO aiAnalyze(Long resumeId, Long userId) {
        checkResumeAccess(resumeId, userId);

        String resumeMarkdown = buildResumeMarkdown(resumeId);
        // DashScope Qwen 输入限制约 30720 字符，预留 prompt 和响应空间
        if (resumeMarkdown.length() > 25000) {
            resumeMarkdown = resumeMarkdown.substring(0, 25000) + "\n\n[简历内容过长，已截断]";
        }

        String prompt = """
            你是一位资深 HR 和简历优化专家。请分析以下简历，给出详细评分和建议。
            以 JSON 格式返回：{"overallScore": 85, "formatScore": 90, "contentScore": 80,
            "keywordScore": 85, "strengths": "...", "weaknesses": "...", "suggestions": "..."}

            简历内容：
            %s
            """.formatted(resumeMarkdown);

        String result = jobApp.doChat(prompt, "resume-analyze-" + resumeId);

        // 解析 AI 返回的 JSON 结果
        AiResumeAnalysis analysis = new AiResumeAnalysis();
        analysis.setResumeId(resumeId);
        analysis.setSeekerId(userId);
        try {
            String json = extractJson(result);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            analysis.setOverallScore(toInt(map.get("overallScore")));
            analysis.setFormatScore(toInt(map.get("formatScore")));
            analysis.setContentScore(toInt(map.get("contentScore")));
            analysis.setKeywordScore(toInt(map.get("keywordScore")));
            analysis.setStrengths(toString(map.get("strengths")));
            analysis.setWeaknesses(toString(map.get("weaknesses")));
            analysis.setSuggestions(toString(map.get("suggestions")));
            analysis.setOptimizedContent(toString(map.get("optimizedContent")));
        } catch (Exception e) {
            log.warn("AI 分析结果解析失败，保存原始结果: {}", e.getMessage());
            analysis.setSuggestions(result);
        }
        analysisMapper.insert(analysis);

        log.info("AI 分析完成: resumeId={}, overallScore={}", resumeId, analysis.getOverallScore());

        ResumeAnalysisVO vo = new ResumeAnalysisVO();
        vo.setId(analysis.getId());
        vo.setResumeId(resumeId);
        vo.setOverallScore(analysis.getOverallScore());
        vo.setFormatScore(analysis.getFormatScore());
        vo.setContentScore(analysis.getContentScore());
        vo.setKeywordScore(analysis.getKeywordScore());
        vo.setStrengths(analysis.getStrengths());
        vo.setWeaknesses(analysis.getWeaknesses());
        vo.setSuggestions(analysis.getSuggestions());
        vo.setOptimizedContent(analysis.getOptimizedContent());
        vo.setCreatedAt(analysis.getCreatedAt());
        return vo;
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private String toString(Object val) {
        return val != null ? val.toString() : null;
    }

    // ==================== AI 优化 ====================

    @Override
    public String aiOptimize(Long resumeId, Long userId) {
        checkResumeAccess(resumeId, userId);
        String resumeMarkdown = buildResumeMarkdown(resumeId);
        if (resumeMarkdown.length() > 25000) {
            resumeMarkdown = resumeMarkdown.substring(0, 25000) + "\n\n[简历内容过长，已截断]";
        }

        String prompt = """
            你是一位顶级简历优化顾问。请根据以下简历内容，提供优化建议。
            对每个字段给出「原文」和「优化后」的对比及简短说明。
            以 JSON 格式返回：{"suggestions": [{"field": "...", "original": "...",
            "optimized": "...", "reason": "..."}]}

            简历内容：
            %s
            """.formatted(resumeMarkdown);

        return jobApp.doChat(prompt, "resume-optimize-" + resumeId);
    }

    // ==================== PDF 导出 ====================

    @Override
    public org.springframework.core.io.Resource exportPdf(Long resumeId, Long userId) {
        checkResumeAccess(resumeId, userId);
        ResumeVO vo = getResumeDetail(resumeId, userId);

        StringBuilder sb = new StringBuilder();
        sb.append("========== 个人简历 ==========\n\n");
        sb.append("姓名: ").append(vo.getFullName()).append("\n");
        sb.append("电话: ").append(nullToEmpty(vo.getPhone())).append("\n");
        sb.append("邮箱: ").append(nullToEmpty(vo.getEmail())).append("\n");
        sb.append("现居: ").append(nullToEmpty(vo.getCurrentCity()));
        sb.append(" | 期望城市: ").append(nullToEmpty(vo.getExpectedCity())).append("\n");
        if (vo.getExpectedSalaryMin() != null) {
            sb.append("期望薪资: ").append(vo.getExpectedSalaryMin())
              .append("K - ").append(vo.getExpectedSalaryMax()).append("K\n");
        }
        sb.append("\n---------- 自我评价 ----------\n");
        sb.append(nullToEmpty(vo.getSummary())).append("\n");

        sb.append("\n---------- 教育经历 ----------\n");
        for (ResumeEducation edu : vo.getEducations()) {
            sb.append(edu.getSchoolName()).append(" | ")
              .append(nullToEmpty(edu.getDegree())).append(" | ")
              .append(nullToEmpty(edu.getMajor())).append(" | ")
              .append(formatDate(edu.getStartDate())).append(" - ")
              .append(formatDate(edu.getEndDate())).append("\n");
            if (StringUtils.isNotBlank(edu.getDescription())) {
                sb.append("  ").append(edu.getDescription()).append("\n");
            }
        }

        sb.append("\n---------- 工作经历 ----------\n");
        for (ResumeWorkExperience work : vo.getWorkExperiences()) {
            sb.append(work.getCompanyName()).append(" | ")
              .append(nullToEmpty(work.getPosition())).append(" | ")
              .append(formatDate(work.getStartDate())).append(" - ")
              .append(formatDate(work.getEndDate())).append("\n");
            if (StringUtils.isNotBlank(work.getDescription())) {
                sb.append("  ").append(work.getDescription()).append("\n");
            }
        }

        sb.append("\n---------- 项目经历 ----------\n");
        for (ResumeProject proj : vo.getProjects()) {
            sb.append(proj.getProjectName()).append(" | ")
              .append(nullToEmpty(proj.getRole())).append("\n");
            if (StringUtils.isNotBlank(proj.getDescription())) {
                sb.append("  ").append(proj.getDescription()).append("\n");
            }
        }

        sb.append("\n---------- 技能 ----------\n");
        for (ResumeSkill skill : vo.getSkills()) {
            sb.append("- ").append(skill.getSkillName());
            if (StringUtils.isNotBlank(skill.getProficiency())) {
                sb.append(" (").append(skill.getProficiency()).append(")");
            }
            sb.append("\n");
        }

        String fileName = "resume_" + resumeId + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = FileConstant.FILE_SAVE_DIR + File.separator + fileName;
        File dir = new File(FileConstant.FILE_SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
        PdfUtil.createPdf(filePath, sb.toString());

        return new FileSystemResource(filePath);
    }

    // ==================== PDF 上传解析 ====================

    @Override
    public long uploadAndParsePdf(Long userId, MultipartFile file) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "请选择PDF文件");

        // 保存临时文件
        String tmpDir = FileConstant.FILE_SAVE_DIR + File.separator + "uploads";
        File dir = new File(tmpDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = "upload_" + userId + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = tmpDir + File.separator + fileName;
        try {
            file.transferTo(new File(filePath));
        } catch (Exception e) {
            log.error("PDF 保存失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }

        // 提取 PDF 文本
        String pdfText;
        try {
            pdfText = PdfUtil.extractText(filePath);
        } catch (Exception e) {
            log.error("PDF 解析失败", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "PDF 解析失败，请确认文件为非扫描件");
        }

        ThrowUtils.throwIf(StringUtils.isBlank(pdfText),
                ErrorCode.PARAMS_ERROR, "未能从 PDF 中提取到文字内容");

        // AI 解析简历
        String prompt = """
            你是简历解析专家。从以下 PDF 原始文本中提取简历信息，以 JSON 返回：
            {"title":"简历标题","fullName":"姓名","phone":"电话","email":"邮箱",
            "currentCity":"现居城市","expectedCity":"期望城市",
            "expectedSalaryMin":0,"expectedSalaryMax":0,
            "jobStatus":"在职/离职/应届","summary":"自我评价",
            "educations":[{"schoolName":"","degree":"","major":"","startDate":"","endDate":"","description":""}],
            "works":[{"companyName":"","position":"","startDate":"","endDate":"","description":""}],
            "skills":[{"skillName":"","proficiency":"","monthsOfUse":0}]}

            只返回 JSON，不要附加说明。

            PDF 文本：
            %s
            """.formatted(pdfText.length() > 6000 ? pdfText.substring(0, 6000) : pdfText);

        String jsonResult = jobApp.doChat(prompt, "pdf-parse-" + userId);

        // 创建简历
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle("来自PDF导入");
        resume.setFullName("PDF导入");
        resume.setPrivacy("PRIVATE");
        resume.setIsDefault(false);
        // 保持简单，用 AI 返回的第一个候选 title
        if (StringUtils.isNotBlank(jsonResult)) {
            // 简单提取可能的标题
            if (jsonResult.contains("\"title\":\"")) {
                String title = jsonResult.replaceAll(".*\"title\":\"([^\"]+)\".*", "$1");
                if (title.length() < 50) resume.setTitle(title);
            }
            if (jsonResult.contains("\"fullName\":\"")) {
                String name = jsonResult.replaceAll(".*\"fullName\":\"([^\"]+)\".*", "$1");
                if (name.length() < 20) resume.setFullName(name);
            }
            if (jsonResult.contains("\"phone\":\"")) {
                String phone = jsonResult.replaceAll(".*\"phone\":\"([^\"]+)\".*", "$1");
                if (phone.length() < 20) resume.setPhone(phone);
            }
            if (jsonResult.contains("\"email\":\"")) {
                String email = jsonResult.replaceAll(".*\"email\":\"([^\"]+)\".*", "$1");
                if (email.length() < 60) resume.setEmail(email);
            }
        }

        this.save(resume);
        log.info("PDF 简历导入: id={}, userId={}", resume.getId(), userId);
        return resume.getId();
    }

    // ==================== 辅助方法 ====================

    @Override
    public ResumeVO toResumeVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setUserId(resume.getUserId());
        vo.setTitle(resume.getTitle());
        vo.setFullName(resume.getFullName());
        vo.setEmail(resume.getEmail());
        vo.setPhone(resume.getPhone());
        vo.setCurrentCity(resume.getCurrentCity());
        vo.setExpectedCity(resume.getExpectedCity());
        vo.setExpectedSalaryMin(resume.getExpectedSalaryMin());
        vo.setExpectedSalaryMax(resume.getExpectedSalaryMax());
        vo.setJobStatus(resume.getJobStatus());
        vo.setSummary(resume.getSummary());
        vo.setPrivacy(resume.getPrivacy());
        vo.setIsDefault(resume.getIsDefault());
        vo.setAiScore(resume.getAiScore());
        vo.setAiSuggestion(resume.getAiSuggestion());
        vo.setCreatedAt(resume.getCreatedAt());
        vo.setUpdatedAt(resume.getUpdatedAt());
        return vo;
    }

    @Override
    public String buildResumeMarkdown(Long resumeId) {
        Resume resume = this.getById(resumeId);
        if (resume == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("# 个人简历\n\n");
        sb.append("**姓名:** ").append(nullToEmpty(resume.getFullName())).append("\n");
        sb.append("**城市:** ").append(nullToEmpty(resume.getCurrentCity()));
        sb.append(" | **期望:** ").append(nullToEmpty(resume.getExpectedCity())).append("\n\n");

        sb.append("## 自我评价\n").append(nullToEmpty(resume.getSummary())).append("\n\n");

        List<ResumeEducation> educations = listEducations(resumeId);
        if (!educations.isEmpty()) {
            sb.append("## 教育经历\n");
            for (ResumeEducation e : educations) {
                sb.append("- ").append(e.getSchoolName()).append(" | ")
                  .append(nullToEmpty(e.getDegree())).append(" | ")
                  .append(nullToEmpty(e.getMajor())).append("\n");
            }
            sb.append("\n");
        }

        List<ResumeWorkExperience> works = listWorkExperiences(resumeId);
        if (!works.isEmpty()) {
            sb.append("## 工作经历\n");
            for (ResumeWorkExperience w : works) {
                sb.append("- **").append(w.getCompanyName()).append("** - ")
                  .append(nullToEmpty(w.getPosition())).append("\n");
                if (StringUtils.isNotBlank(w.getDescription())) {
                    sb.append("  ").append(w.getDescription()).append("\n");
                }
            }
            sb.append("\n");
        }

        List<ResumeProject> projects = listProjects(resumeId);
        if (!projects.isEmpty()) {
            sb.append("## 项目经历\n");
            for (ResumeProject p : projects) {
                sb.append("- **").append(p.getProjectName()).append("**")
                  .append(nullToEmpty(p.getRole())).append("\n");
                if (StringUtils.isNotBlank(p.getDescription())) {
                    sb.append("  ").append(p.getDescription()).append("\n");
                }
            }
            sb.append("\n");
        }

        List<ResumeSkill> skills = listSkills(resumeId);
        if (!skills.isEmpty()) {
            sb.append("## 技能\n");
            for (ResumeSkill s : skills) {
                sb.append("- ").append(s.getSkillName());
                if (StringUtils.isNotBlank(s.getProficiency())) {
                    sb.append(" (").append(s.getProficiency()).append(")");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public void checkResumeAccess(Long resumeId, Long userId) {
        Resume resume = this.getById(resumeId);
        ThrowUtils.throwIf(resume == null, ErrorCode.NOT_FOUND_ERROR, "简历不存在");
        ThrowUtils.throwIf(!resume.getUserId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "无权操作此简历");
    }

    // ==================== 内部查询 ====================

    private List<ResumeEducation> listEducations(Long resumeId) {
        LambdaQueryWrapper<ResumeEducation> w = new LambdaQueryWrapper<>();
        w.eq(ResumeEducation::getResumeId, resumeId).orderByAsc(ResumeEducation::getSortOrder);
        return educationMapper.selectList(w);
    }

    private List<ResumeWorkExperience> listWorkExperiences(Long resumeId) {
        LambdaQueryWrapper<ResumeWorkExperience> w = new LambdaQueryWrapper<>();
        w.eq(ResumeWorkExperience::getResumeId, resumeId).orderByAsc(ResumeWorkExperience::getSortOrder);
        return workMapper.selectList(w);
    }

    private List<ResumeProject> listProjects(Long resumeId) {
        LambdaQueryWrapper<ResumeProject> w = new LambdaQueryWrapper<>();
        w.eq(ResumeProject::getResumeId, resumeId).orderByAsc(ResumeProject::getSortOrder);
        return projectMapper.selectList(w);
    }

    private List<ResumeSkill> listSkills(Long resumeId) {
        LambdaQueryWrapper<ResumeSkill> w = new LambdaQueryWrapper<>();
        w.eq(ResumeSkill::getResumeId, resumeId).orderByAsc(ResumeSkill::getSortOrder);
        return skillMapper.selectList(w);
    }

    private List<ResumeCertificate> listCertificates(Long resumeId) {
        LambdaQueryWrapper<ResumeCertificate> w = new LambdaQueryWrapper<>();
        w.eq(ResumeCertificate::getResumeId, resumeId).orderByAsc(ResumeCertificate::getSortOrder);
        return certMapper.selectList(w);
    }

    private void deleteByResumeId(String type, Long resumeId) {
        switch (type) {
            case "education" -> educationMapper.delete(
                    new LambdaQueryWrapper<ResumeEducation>().eq(ResumeEducation::getResumeId, resumeId));
            case "work" -> workMapper.delete(
                    new LambdaQueryWrapper<ResumeWorkExperience>().eq(ResumeWorkExperience::getResumeId, resumeId));
            case "project" -> projectMapper.delete(
                    new LambdaQueryWrapper<ResumeProject>().eq(ResumeProject::getResumeId, resumeId));
            case "skill" -> skillMapper.delete(
                    new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resumeId));
            case "cert" -> certMapper.delete(
                    new LambdaQueryWrapper<ResumeCertificate>().eq(ResumeCertificate::getResumeId, resumeId));
        }
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "至今";
    }

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }
}
