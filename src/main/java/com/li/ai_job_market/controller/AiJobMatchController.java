package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.model.entity.AiJobMatch;
import com.li.ai_job_market.model.vo.UserVO;
import com.li.ai_job_market.service.AiJobMatchService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI职位匹配控制器 —— 提供简历与职位的智能匹配计算及匹配结果查询功能
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiJobMatchController {

    @Resource private AiJobMatchService aiJobMatchService;
    @Resource private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    // 计算简历与职位的AI匹配度
    @PostMapping("/match/compute")
    public BaseResponse<String> compute(@RequestParam Long resumeId,
                                        @RequestParam Long jobId,
                                        HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        aiJobMatchService.computeMatch(user.getId(), resumeId, jobId);
        return ResultUtils.success("匹配计算完成");
    }

    // 获取当前用户的匹配记录列表
    @GetMapping("/match/my")
    public BaseResponse<List<AiJobMatch>> myMatches(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiJobMatchService.listBySeeker(user.getId()));
    }

    // 分页获取当前用户的匹配记录
    @GetMapping("/match/page")
    public BaseResponse<Page<AiJobMatch>> page(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiJobMatchService.pageBySeeker(user.getId(), current, size));
    }
}
