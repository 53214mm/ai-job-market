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

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiJobMatchController {

    @Resource private AiJobMatchService aiJobMatchService;
    @Resource private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    @PostMapping("/match/compute")
    public BaseResponse<String> compute(@RequestParam Long resumeId,
                                        @RequestParam Long jobId,
                                        HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        aiJobMatchService.computeMatch(user.getId(), resumeId, jobId);
        return ResultUtils.success("匹配计算完成");
    }

    @GetMapping("/match/my")
    public BaseResponse<List<AiJobMatch>> myMatches(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiJobMatchService.listBySeeker(user.getId()));
    }

    @GetMapping("/match/page")
    public BaseResponse<Page<AiJobMatch>> page(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiJobMatchService.pageBySeeker(user.getId(), current, size));
    }
}
