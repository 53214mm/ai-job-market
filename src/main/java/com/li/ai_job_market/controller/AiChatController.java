package com.li.ai_job_market.controller;

import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.model.entity.AiChatMessage;
import com.li.ai_job_market.model.entity.AiChatSession;
import com.li.ai_job_market.model.entity.AiInterviewRecord;
import com.li.ai_job_market.model.vo.UserVO;
import com.li.ai_job_market.AI.app.JobApp;
import com.li.ai_job_market.service.AiChatService;
import com.li.ai_job_market.service.AiInterviewService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * AI聊天控制器 —— 提供AI聊天会话管理、流式对话及AI模拟面试功能
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Resource private AiChatService aiChatService;
    @Resource private AiInterviewService aiInterviewService;
    @Resource private UserService userService;
    @Resource private JobApp jobApp;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    // ==================== AI 聊天 ====================

    // 创建AI聊天会话
    @PostMapping("/chat/sessions")
    public BaseResponse<Long> createSession(@RequestBody CreateSessionReq req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiChatService.createSession(user.getId(), req.getType(), req.getTitle()));
    }

    // 获取当前用户的聊天会话列表
    @GetMapping("/chat/sessions")
    public BaseResponse<List<AiChatSession>> listSessions(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiChatService.listSessions(user.getId()));
    }

    // 获取指定会话的聊天消息
    @GetMapping("/chat/sessions/{id}/messages")
    public BaseResponse<List<AiChatMessage>> messages(@PathVariable Long id) {
        return ResultUtils.success(aiChatService.getMessages(id));
    }

    // 发送聊天消息（非流式）
    @PostMapping("/chat/send")
    public BaseResponse<String> sendMessage(@RequestBody SendMessageReq req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiChatService.sendMessage(req.getSessionId(), user.getId(), req.getMessage()));
    }

    // ==================== 流式聊天 ====================

    // SSE流式AI聊天
    @GetMapping("/chat/stream")
    public SseEmitter chatStream(@RequestParam String message,
                                  @RequestParam(defaultValue = "recruitment-chat") String chatId,
                                  @RequestParam(required = false) String token,
                                  HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);

        jobApp.doChatByStream(message, chatId).subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                () -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data(""));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }
        );

        return emitter;
    }

    // ==================== AI 模拟面试 ====================

    // 开始AI模拟面试
    @PostMapping("/interview/start")
    public BaseResponse<AiInterviewRecord> startInterview(@RequestBody InterviewStartReq req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiInterviewService.startInterview(user.getId(), req.getJobId()));
    }

    // 提交面试答案并获取AI评价
    @PostMapping("/interview/answer")
    public BaseResponse<Map<String, Object>> answerQuestion(@RequestBody InterviewAnswerReq req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(aiInterviewService.evaluateAnswer(req.getRecordId(), user.getId(), req.getAnswer()));
    }

    // 获取AI模拟面试报告
    @GetMapping("/interview/{sessionId}/report")
    public BaseResponse<Map<String, Object>> getReport(@PathVariable Long sessionId) {
        return ResultUtils.success(aiInterviewService.getReport(sessionId));
    }
}

// ---------- Inner DTOs ----------
@Data class CreateSessionReq { private String type; private String title; }
@Data class SendMessageReq { private Long sessionId; private String message; }
@Data class InterviewStartReq { private Long jobId; }
@Data class InterviewAnswerReq { private Long recordId; private String answer; }
