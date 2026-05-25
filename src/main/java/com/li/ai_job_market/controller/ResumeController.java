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

    @PostMapping
    public BaseResponse<Long> create(@RequestBody ResumeCreateRequest req,
                                      HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        long resumeId = resumeService.createResume(userId, req);
        return ResultUtils.success(resumeId);
    }

    @GetMapping
    public BaseResponse<Page<Resume>> list(ResumeQueryRequest req,
                                            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        Page<Resume> page = resumeService.listMyResumes(userId, req);
        return ResultUtils.success(page);
    }

    @GetMapping("/{id}")
    public BaseResponse<ResumeVO> detail(@PathVariable Long id,
                                          HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeVO vo = resumeService.getResumeDetail(id, userId);
        return ResultUtils.success(vo);
    }

    @PutMapping("/{id}")
    public BaseResponse<Boolean> update(@PathVariable Long id,
                                         @RequestBody ResumeUpdateRequest req,
                                         HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.updateResume(id, userId, req);
        return ResultUtils.success(ok);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> delete(@PathVariable Long id,
                                         HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.deleteResume(id, userId);
        return ResultUtils.success(ok);
    }

    @PutMapping("/{id}/default")
    public BaseResponse<Boolean> setDefault(@PathVariable Long id,
                                             HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.setDefault(id, userId);
        return ResultUtils.success(ok);
    }

    // ==================== 教育经历 ====================

    @PostMapping("/{id}/education")
    public BaseResponse<ResumeEducation> addEducation(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeEducation edu = resumeService.addEducation(id, userId, req);
        return ResultUtils.success(edu);
    }

    @PutMapping("/{id}/education/{eid}")
    public BaseResponse<ResumeEducation> updateEducation(
            @PathVariable Long id, @PathVariable Long eid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeEducation edu = resumeService.updateEducation(id, eid, userId, req);
        return ResultUtils.success(edu);
    }

    @DeleteMapping("/{id}/education/{eid}")
    public BaseResponse<Boolean> deleteEducation(
            @PathVariable Long id, @PathVariable Long eid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        boolean ok = resumeService.deleteEducation(id, eid, userId);
        return ResultUtils.success(ok);
    }

    // ==================== 工作经历 ====================

    @PostMapping("/{id}/work")
    public BaseResponse<ResumeWorkExperience> addWork(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addWorkExperience(id, userId, req));
    }

    @PutMapping("/{id}/work/{wid}")
    public BaseResponse<ResumeWorkExperience> updateWork(
            @PathVariable Long id, @PathVariable Long wid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateWorkExperience(id, wid, userId, req));
    }

    @DeleteMapping("/{id}/work/{wid}")
    public BaseResponse<Boolean> deleteWork(
            @PathVariable Long id, @PathVariable Long wid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteWorkExperience(id, wid, userId));
    }

    // ==================== 项目经历 ====================

    @PostMapping("/{id}/project")
    public BaseResponse<ResumeProject> addProject(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addProject(id, userId, req));
    }

    @PutMapping("/{id}/project/{pid}")
    public BaseResponse<ResumeProject> updateProject(
            @PathVariable Long id, @PathVariable Long pid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateProject(id, pid, userId, req));
    }

    @DeleteMapping("/{id}/project/{pid}")
    public BaseResponse<Boolean> deleteProject(
            @PathVariable Long id, @PathVariable Long pid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteProject(id, pid, userId));
    }

    // ==================== 技能 ====================

    @PostMapping("/{id}/skill")
    public BaseResponse<ResumeSkill> addSkill(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addSkill(id, userId, req));
    }

    @PutMapping("/{id}/skill/{sid}")
    public BaseResponse<ResumeSkill> updateSkill(
            @PathVariable Long id, @PathVariable Long sid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateSkill(id, sid, userId, req));
    }

    @DeleteMapping("/{id}/skill/{sid}")
    public BaseResponse<Boolean> deleteSkill(
            @PathVariable Long id, @PathVariable Long sid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteSkill(id, sid, userId));
    }

    // ==================== 证书 ====================

    @PostMapping("/{id}/cert")
    public BaseResponse<ResumeCertificate> addCertificate(
            @PathVariable Long id, @RequestBody SubItemRequest req,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.addCertificate(id, userId, req));
    }

    @PutMapping("/{id}/cert/{cid}")
    public BaseResponse<ResumeCertificate> updateCertificate(
            @PathVariable Long id, @PathVariable Long cid,
            @RequestBody SubItemRequest req, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.updateCertificate(id, cid, userId, req));
    }

    @DeleteMapping("/{id}/cert/{cid}")
    public BaseResponse<Boolean> deleteCertificate(
            @PathVariable Long id, @PathVariable Long cid,
            HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return ResultUtils.success(resumeService.deleteCertificate(id, cid, userId));
    }

    // ==================== AI 功能 ====================

    @PostMapping("/{id}/ai-analyze")
    public BaseResponse<ResumeAnalysisVO> aiAnalyze(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        ResumeAnalysisVO result = resumeService.aiAnalyze(id, userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/{id}/ai-optimize")
    public BaseResponse<String> aiOptimize(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        String result = resumeService.aiOptimize(id, userId);
        return ResultUtils.success(result);
    }

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

    @PostMapping("/upload-pdf")
    public BaseResponse<Long> uploadPdf(@RequestParam("file") MultipartFile file,
                                         HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        long resumeId = resumeService.uploadAndParsePdf(userId, file);
        return ResultUtils.success(resumeId);
    }
}
