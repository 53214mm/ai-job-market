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

/**
 * 职位控制器 —— 提供职位发布、搜索、推荐、状态管理及技能标签管理功能
 */
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

    // 发布职位（仅招聘方）
    @PostMapping
    public BaseResponse<Long> create(@RequestBody JobCreateRequest req,
                                      HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        ThrowUtils.throwIf(!UserRoleEnum.RECRUITER.getValue().equals(loginUser.getRole()),
                ErrorCode.NO_AUTH_ERROR, "仅招聘方可发布职位");
        return ResultUtils.success(jobService.createJob(loginUser.getId(), req));
    }

    // 分页搜索职位
    @GetMapping
    public BaseResponse<Page<JobVO>> list(JobQueryRequest req) {
        return ResultUtils.success(jobService.listJobs(req));
    }

    // 语义搜索职位
    @GetMapping("/search/semantic")
    public BaseResponse<Page<JobVO>> semanticSearch(JobQueryRequest req) {
        return ResultUtils.success(jobService.listJobs(req));
    }

    // 获取我发布的职位（招聘方视角）
    @GetMapping("/my")
    public BaseResponse<Page<JobVO>> myJobs(JobQueryRequest req,
                                             HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.listMyJobs(loginUser.getId(), req));
    }

    // AI智能推荐职位
    @GetMapping("/recommend")
    public BaseResponse<List<JobVO>> recommend(@RequestParam(required = false) Long resumeId,
                                                HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.recommendJobs(loginUser.getId(), resumeId));
    }

    // 获取职位详情
    @GetMapping("/{id}")
    public BaseResponse<JobVO> detail(@PathVariable Long id) {
        return ResultUtils.success(jobService.getJobDetail(id));
    }

    // 更新职位信息
    @PutMapping("/{id}")
    public BaseResponse<Boolean> update(@PathVariable Long id,
                                         @RequestBody JobUpdateRequest req,
                                         HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.updateJob(id, loginUser.getId(), req));
    }

    // 发布职位（上线）
    @PutMapping("/{id}/publish")
    public BaseResponse<Boolean> publish(@PathVariable Long id,
                                          HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.publishJob(id, loginUser.getId()));
    }

    // 关闭职位（下架）
    @PutMapping("/{id}/close")
    public BaseResponse<Boolean> close(@PathVariable Long id,
                                        HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.closeJob(id, loginUser.getId()));
    }

    // 添加职位技能标签
    @PostMapping("/{id}/skill-tags")
    public BaseResponse<JobSkillTag> addSkillTag(@PathVariable Long id,
                                                  @RequestParam String skillName,
                                                  @RequestParam(defaultValue = "true") boolean isRequired,
                                                  HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.addSkillTag(id, loginUser.getId(), skillName, isRequired));
    }

    // 删除职位技能标签
    @DeleteMapping("/{id}/skill-tags/{sid}")
    public BaseResponse<Boolean> deleteSkillTag(@PathVariable Long id,
                                                 @PathVariable Long sid,
                                                 HttpServletRequest request) {
        UserVO loginUser = getLoginUser(request);
        return ResultUtils.success(jobService.deleteSkillTag(id, sid, loginUser.getId()));
    }
}
