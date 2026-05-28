package com.li.ai_job_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.model.dto.message.SendMessageReq;
import com.li.ai_job_market.model.vo.MessageVO;
import com.li.ai_job_market.model.vo.NotificationVO;
import com.li.ai_job_market.model.vo.UserVO;
import com.li.ai_job_market.service.MessageService;
import com.li.ai_job_market.service.NotificationService;
import com.li.ai_job_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("")
public class NotificationController {

    @Resource private NotificationService notificationService;
    @Resource private MessageService messageService;
    @Resource private UserService userService;

    private UserVO getLoginUser(HttpServletRequest request) {
        return userService.getLoginUser(request);
    }

    // ==================== 通知 ====================

    @GetMapping("/notifications")
    public BaseResponse<Page<NotificationVO>> listNotifications(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.listMyNotifications(user.getId(), current, pageSize));
    }

    @GetMapping("/notifications/unread-count")
    public BaseResponse<Map<String, Long>> unreadNotificationCount(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        long count = notificationService.unreadCount(user.getId());
        return ResultUtils.success(Map.of("count", count));
    }

    @PutMapping("/notifications/{id}/read")
    public BaseResponse<Boolean> markNotificationRead(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.markRead(id, user.getId()));
    }

    @PutMapping("/notifications/read-all")
    public BaseResponse<Boolean> markAllNotificationsRead(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.markAllRead(user.getId()));
    }

    @DeleteMapping("/notifications/{id}")
    public BaseResponse<Boolean> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.delete(id, user.getId()));
    }

    // ==================== 私信 ====================

    @PostMapping("/messages")
    public BaseResponse<MessageVO> sendMessage(@RequestBody SendMessageReq req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.send(user.getId(), req.getReceiverId(),
                req.getApplicationId(), req.getContent()));
    }

    @GetMapping("/messages/conversations")
    public BaseResponse<List<MessageVO>> listConversations(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.listConversations(user.getId()));
    }

    @GetMapping("/messages/{peerId}")
    public BaseResponse<Page<MessageVO>> getConversation(
            @PathVariable Long peerId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.getConversation(user.getId(), peerId, current, pageSize));
    }

    @GetMapping("/messages/unread-count")
    public BaseResponse<Map<String, Long>> unreadMessageCount(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        long count = messageService.unreadCount(user.getId());
        return ResultUtils.success(Map.of("count", count));
    }

    @PutMapping("/messages/{id}/read")
    public BaseResponse<Boolean> markMessageRead(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.markRead(id, user.getId()));
    }
}
