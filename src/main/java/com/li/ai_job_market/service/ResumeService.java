package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.dto.resume.ResumeCreateRequest;
import com.li.ai_job_market.model.dto.resume.ResumeQueryRequest;
import com.li.ai_job_market.model.dto.resume.ResumeUpdateRequest;
import com.li.ai_job_market.model.dto.resume.SubItemRequest;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.vo.ResumeAnalysisVO;
import com.li.ai_job_market.model.vo.ResumeVO;
import org.springframework.core.io.Resource;

/** 简历管理服务接口，提供简历全生命周期管理和AI分析优化功能 */
public interface ResumeService extends IService<Resume> {

    // === 简历 CRUD ===
    long createResume(Long userId, ResumeCreateRequest req);
    ResumeVO getResumeDetail(Long resumeId, Long currentUserId);
    boolean updateResume(Long resumeId, Long userId, ResumeUpdateRequest req);
    boolean deleteResume(Long resumeId, Long userId);
    Page<Resume> listMyResumes(Long userId, ResumeQueryRequest req);
    boolean setDefault(Long resumeId, Long userId);

    // === 教育经历 ===
    ResumeEducation addEducation(Long resumeId, Long userId, SubItemRequest req);
    ResumeEducation updateEducation(Long resumeId, Long eid, Long userId, SubItemRequest req);
    boolean deleteEducation(Long resumeId, Long eid, Long userId);

    // === 工作经历 ===
    ResumeWorkExperience addWorkExperience(Long resumeId, Long userId, SubItemRequest req);
    ResumeWorkExperience updateWorkExperience(Long resumeId, Long wid, Long userId, SubItemRequest req);
    boolean deleteWorkExperience(Long resumeId, Long wid, Long userId);

    // === 项目经历 ===
    ResumeProject addProject(Long resumeId, Long userId, SubItemRequest req);
    ResumeProject updateProject(Long resumeId, Long pid, Long userId, SubItemRequest req);
    boolean deleteProject(Long resumeId, Long pid, Long userId);

    // === 技能 ===
    ResumeSkill addSkill(Long resumeId, Long userId, SubItemRequest req);
    ResumeSkill updateSkill(Long resumeId, Long sid, Long userId, SubItemRequest req);
    boolean deleteSkill(Long resumeId, Long sid, Long userId);

    // === 证书 ===
    ResumeCertificate addCertificate(Long resumeId, Long userId, SubItemRequest req);
    ResumeCertificate updateCertificate(Long resumeId, Long cid, Long userId, SubItemRequest req);
    boolean deleteCertificate(Long resumeId, Long cid, Long userId);

    // === AI 功能 ===
    ResumeAnalysisVO aiAnalyze(Long resumeId, Long userId);
    String aiOptimize(Long resumeId, Long userId);

    // === PDF ===
    Resource exportPdf(Long resumeId, Long userId);
    long uploadAndParsePdf(Long userId, org.springframework.web.multipart.MultipartFile file);

    // === 工具 ===
    ResumeVO toResumeVO(Resume resume);
    String buildResumeMarkdown(Long resumeId);
    void checkResumeAccess(Long resumeId, Long userId);
}
