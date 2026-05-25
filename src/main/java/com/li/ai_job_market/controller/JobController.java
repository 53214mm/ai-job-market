package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.model.dto.job.*;
import com.li.ai_job_market.model.entity.JobSkillTag;
import com.li.ai_job_market.model.enums.UserRoleEnum;
import com.li.ai_job_market.model.vo.*;
import com.li.ai_job_market.service.JobService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Resource
    private JobService jobService;
    @Resource
    private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    @PostMapping
    public BaseResponse<Long> create(@RequestBody JobCreateRequest req,
                                      HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        ThrowUtils.throwIf(!UserRoleEnum.RECRUITER.getValue().equals(loginUser.getRole()),
                ErrorCode.NO_AUTH_ERROR, "仅招聘方可发布职位");
        return ResultUtils.success(jobService.createJob(loginUser.getId(), req));
    }

    @GetMapping
    public BaseResponse<Page<JobVO>> list(JobQueryRequest req) {
        return ResultUtils.success(jobService.listJobs(req));
    }

    @GetMapping("/search/semantic")
    public BaseResponse<Page<JobVO>> semanticSearch(JobQueryRequest req) {
        return ResultUtils.success(jobService.listJobs(req));
    }

    @GetMapping("/my")
    public BaseResponse<Page<JobVO>> myJobs(JobQueryRequest req,
                                             HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.listMyJobs(loginUser.getId(), req));
    }

    @GetMapping("/recommend")
    public BaseResponse<List<JobVO>> recommend(@RequestParam(required = false) Long resumeId,
                                                HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.recommendJobs(loginUser.getId(), resumeId));
    }

    @GetMapping("/{id}")
    public BaseResponse<JobVO> detail(@PathVariable Long id) {
        return ResultUtils.success(jobService.getJobDetail(id));
    }

    @PutMapping("/{id}")
    public BaseResponse<Boolean> update(@PathVariable Long id,
                                         @RequestBody JobUpdateRequest req,
                                         HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.updateJob(id, loginUser.getId(), req));
    }

    @PutMapping("/{id}/publish")
    public BaseResponse<Boolean> publish(@PathVariable Long id,
                                          HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.publishJob(id, loginUser.getId()));
    }

    @PutMapping("/{id}/close")
    public BaseResponse<Boolean> close(@PathVariable Long id,
                                        HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.closeJob(id, loginUser.getId()));
    }

    @PostMapping("/{id}/skill-tags")
    public BaseResponse<JobSkillTag> addSkillTag(@PathVariable Long id,
                                                  @RequestParam String skillName,
                                                  @RequestParam(defaultValue = "true") boolean isRequired,
                                                  HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.addSkillTag(id, loginUser.getId(), skillName, isRequired));
    }

    @DeleteMapping("/{id}/skill-tags/{sid}")
    public BaseResponse<Boolean> deleteSkillTag(@PathVariable Long id,
                                                 @PathVariable Long sid,
                                                 HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.deleteSkillTag(id, sid, loginUser.getId()));
    }
}
