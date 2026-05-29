package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.model.dto.resume.*;
import com.li.ai_job_market.model.entity.*;
import com.li.ai_job_market.model.enums.UserRoleEnum;
import com.li.ai_job_market.model.vo.*;
import com.li.ai_job_market.service.ResumeService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历控制器 —— 提供简历CRUD、教育/工作/项目经历管理、技能与证书管理、AI分析与优化、PDF导出及上传解析功能
 */
@Slf4j
@RestController
@RequestMapping("/resumes")
public class ResumeController {

    @Resource
    private ResumeService resumeService;

    @Resource
    private UserService userService;

    private Long getLoginUserId(HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        ThrowUtils.throwIf(!UserRoleEnum.SEEKER.getValue().equals(user.getRole()),
                ErrorCode.NO_AUTH_ERROR, "仅求职者可操作简历");
        return user.getId();
    }

    // ==================== 简历 CRUD ====================

    // 创建简历
    @PostMapping
    public BaseResponse<Long> create(@RequestBody ResumeCreateRequest req,
                                      HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        long resumeId = resumeService.createResume(userId, req);
        return ResultUtils.success(resumeId);
    }

    // 分页获取我的简历列表
    @GetMapping
    public BaseResponse<Page<Resume>> list(ResumeQueryRequest req,
                                            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        Page<Resume> page = resumeService.listMyResumes(userId, req);
        return ResultUtils.success(page);
    }

    // 获取简历详情
    @GetMapping("/{id}")
    public BaseResponse<ResumeVO> detail(@PathVariable Long id,
                                          HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeVO vo = resumeService.getResumeDetail(id, userId);
        return ResultUtils.success(vo);
    }

    // 更新简历
    @PutMapping("/{id}")
    public BaseResponse<Boolean> update(@PathVariable Long id,
                                         @RequestBody ResumeUpdateRequest req,
                                         HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.updateResume(id, userId, req);
        return ResultUtils.success(ok);
    }

    // 删除简历
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> delete(@PathVariable Long id,
                                         HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.deleteResume(id, userId);
        return ResultUtils.success(ok);
    }

    // 设为默认简历
    @PutMapping("/{id}/default")
    public BaseResponse<Boolean> setDefault(@PathVariable Long id,
                                             HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.setDefault(id, userId);
        return ResultUtils.success(ok);
    }

    // ==================== 教育经历 ====================

    // 添加教育经历
    @PostMapping("/{id}/education")
    public BaseResponse<ResumeEducation> addEducation(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeEducation edu = resumeService.addEducation(id, userId, req);
        return ResultUtils.success(edu);
    }

    // 更新教育经历
    @PutMapping("/{id}/education/{eid}")
    public BaseResponse<ResumeEducation> updateEducation(
            @PathVariable Long id, @PathVariable Long eid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeEducation edu = resumeService.updateEducation(id, eid, userId, req);
        return ResultUtils.success(edu);
    }

    // 删除教育经历
    @DeleteMapping("/{id}/education/{eid}")
    public BaseResponse<Boolean> deleteEducation(
            @PathVariable Long id, @PathVariable Long eid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.deleteEducation(id, eid, userId);
        return ResultUtils.success(ok);
    }

    // ==================== 工作经历 ====================

    // 添加工作经历
    @PostMapping("/{id}/work")
    public BaseResponse<ResumeWorkExperience> addWork(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addWorkExperience(id, userId, req));
    }

    // 更新工作经历
    @PutMapping("/{id}/work/{wid}")
    public BaseResponse<ResumeWorkExperience> updateWork(
            @PathVariable Long id, @PathVariable Long wid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateWorkExperience(id, wid, userId, req));
    }

    // 删除工作经历
    @DeleteMapping("/{id}/work/{wid}")
    public BaseResponse<Boolean> deleteWork(
            @PathVariable Long id, @PathVariable Long wid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteWorkExperience(id, wid, userId));
    }

    // ==================== 项目经历 ====================

    // 添加项目经历
    @PostMapping("/{id}/project")
    public BaseResponse<ResumeProject> addProject(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addProject(id, userId, req));
    }

    // 更新项目经历
    @PutMapping("/{id}/project/{pid}")
    public BaseResponse<ResumeProject> updateProject(
            @PathVariable Long id, @PathVariable Long pid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateProject(id, pid, userId, req));
    }

    // 删除项目经历
    @DeleteMapping("/{id}/project/{pid}")
    public BaseResponse<Boolean> deleteProject(
            @PathVariable Long id, @PathVariable Long pid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteProject(id, pid, userId));
    }

    // ==================== 技能 ====================

    // 添加技能
    @PostMapping("/{id}/skill")
    public BaseResponse<ResumeSkill> addSkill(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addSkill(id, userId, req));
    }

    // 更新技能
    @PutMapping("/{id}/skill/{sid}")
    public BaseResponse<ResumeSkill> updateSkill(
            @PathVariable Long id, @PathVariable Long sid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateSkill(id, sid, userId, req));
    }

    // 删除技能
    @DeleteMapping("/{id}/skill/{sid}")
    public BaseResponse<Boolean> deleteSkill(
            @PathVariable Long id, @PathVariable Long sid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteSkill(id, sid, userId));
    }

    // ==================== 证书 ====================

    // 添加证书
    @PostMapping("/{id}/cert")
    public BaseResponse<ResumeCertificate> addCertificate(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addCertificate(id, userId, req));
    }

    // 更新证书
    @PutMapping("/{id}/cert/{cid}")
    public BaseResponse<ResumeCertificate> updateCertificate(
            @PathVariable Long id, @PathVariable Long cid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateCertificate(id, cid, userId, req));
    }

    // 删除证书
    @DeleteMapping("/{id}/cert/{cid}")
    public BaseResponse<Boolean> deleteCertificate(
            @PathVariable Long id, @PathVariable Long cid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteCertificate(id, cid, userId));
    }

    // ==================== AI 功能 ====================

    // AI分析简历
    @PostMapping("/{id}/ai-analyze")
    public BaseResponse<ResumeAnalysisVO> aiAnalyze(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeAnalysisVO result = resumeService.aiAnalyze(id, userId);
        return ResultUtils.success(result);
    }

    // AI优化简历内容
    @PostMapping("/{id}/ai-optimize")
    public BaseResponse<String> aiOptimize(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        String result = resumeService.aiOptimize(id, userId);
        return ResultUtils.success(result);
    }

    // 导出简历为PDF文件
    @PostMapping("/{id}/export-pdf")
    public ResponseEntity<org.springframework.core.io.Resource> exportPdf(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        org.springframework.core.io.Resource resource = resumeService.exportPdf(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.pdf\"")
                .body(resource);
    }

    // ==================== PDF 上传 ====================

    // 上传PDF文件并解析简历
    @PostMapping("/upload-pdf")
    public BaseResponse<Long> uploadPdf(@RequestParam("file") MultipartFile file,
                                         HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        long resumeId = resumeService.uploadAndParsePdf(userId, file);
        return ResultUtils.success(resumeId);
    }
}
