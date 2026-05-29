package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.model.dto.application.*;
import com.li.ai_job_market.model.entity.Interview;
import com.li.ai_job_market.model.vo.*;
import com.li.ai_job_market.service.ApplicationService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投递控制器 —— 提供职位投递、投递状态管理、面试安排及收藏功能
 */
@Slf4j
@RestController
public class ApplicationController {

    @Resource private ApplicationService applicationService;
    @Resource private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    // ==================== 投递 ====================

    // 投递职位
    @PostMapping("/applications")
    public BaseResponse<Long> apply(@RequestBody ApplicationRequest req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.apply(user.getId(), req.getJobId(), req.getResumeId(), req.getCoverLetter()));
    }

    // 分页获取我的投递记录（求职者视角）
    @GetMapping("/applications/my")
    public BaseResponse<Page<ApplicationVO>> my(@RequestParam(defaultValue = "1") int current,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) String status,
                                                 HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.listMyApplications(user.getId(), current, size, status));
    }

    // 分页获取收到的投递（招聘方视角）
    @GetMapping("/applications/received")
    public BaseResponse<Page<ApplicationVO>> received(@RequestParam(defaultValue = "1") int current,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) String status,
                                                       HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.listReceivedApplications(user.getId(), current, size, status));
    }

    // 获取投递详情
    @GetMapping("/applications/{id}")
    public BaseResponse<ApplicationVO> detail(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.getDetail(id, user.getId()));
    }

    // 更新投递状态（如：通过/拒绝）
    @PutMapping("/applications/{id}/status")
    public BaseResponse<Boolean> updateStatus(@PathVariable Long id,
                                               @RequestBody ApplicationStatusRequest req,
                                               HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.updateStatus(id, user.getId(), req.getStatus(), req.getRemark()));
    }

    // 安排面试
    @PostMapping("/applications/{id}/interview")
    public BaseResponse<Interview> scheduleInterview(@PathVariable Long id,
                                                      @RequestBody InterviewRequest req,
                                                      HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.scheduleInterview(id, user.getId(), req));
    }

    // 添加面试反馈
    @PutMapping("/interviews/{iid}/feedback")
    public BaseResponse<Boolean> addFeedback(@PathVariable Long iid,
                                              @RequestParam String feedback,
                                              HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.updateInterviewFeedback(iid, user.getId(), feedback));
    }

    // ==================== 收藏 ====================

    // 添加收藏
    @PostMapping("/favorites")
    public BaseResponse<Long> addFavorite(@RequestBody FavoriteRequest req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.addFavorite(user.getId(), req.getTargetType(), req.getTargetId()));
    }

    // 取消收藏
    @DeleteMapping("/favorites/{id}")
    public BaseResponse<Boolean> removeFavorite(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.removeFavorite(id, user.getId()));
    }

    // 获取收藏列表
    @GetMapping("/favorites")
    public BaseResponse<List<FavoriteVO>> listFavorites(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(applicationService.listFavorites(user.getId()));
    }
}
