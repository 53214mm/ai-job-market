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

/**
 * 通知与私信控制器 —— 提供系统通知管理和用户私信（会话列表、已读标记）功能
 */
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

    // 分页获取通知列表
    @GetMapping("/notifications")
    public BaseResponse<Page<NotificationVO>> listNotifications(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.listMyNotifications(user.getId(), current, pageSize));
    }

    // 获取未读通知数量
    @GetMapping("/notifications/unread-count")
    public BaseResponse<Map<String, Long>> unreadNotificationCount(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        long count = notificationService.unreadCount(user.getId());
        return ResultUtils.success(Map.of("count", count));
    }

    // 标记单个通知为已读
    @PutMapping("/notifications/{id}/read")
    public BaseResponse<Boolean> markNotificationRead(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.markRead(id, user.getId()));
    }

    // 标记所有通知为已读
    @PutMapping("/notifications/read-all")
    public BaseResponse<Boolean> markAllNotificationsRead(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.markAllRead(user.getId()));
    }

    // 删除通知
    @DeleteMapping("/notifications/{id}")
    public BaseResponse<Boolean> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(notificationService.delete(id, user.getId()));
    }

    // ==================== 私信 ====================

    // 发送私信
    @PostMapping("/messages")
    public BaseResponse<MessageVO> sendMessage(@RequestBody SendMessageReq req, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.send(user.getId(), req.getReceiverId(),
                req.getApplicationId(), req.getContent()));
    }

    // 获取会话列表
    @GetMapping("/messages/conversations")
    public BaseResponse<List<MessageVO>> listConversations(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.listConversations(user.getId()));
    }

    // 分页获取与指定用户的私信记录
    @GetMapping("/messages/{peerId}")
    public BaseResponse<Page<MessageVO>> getConversation(
            @PathVariable Long peerId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.getConversation(user.getId(), peerId, current, pageSize));
    }

    // 获取未读私信数量
    @GetMapping("/messages/unread-count")
    public BaseResponse<Map<String, Long>> unreadMessageCount(HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        long count = messageService.unreadCount(user.getId());
        return ResultUtils.success(Map.of("count", count));
    }

    // 标记私信为已读
    @PutMapping("/messages/{id}/read")
    public BaseResponse<Boolean> markMessageRead(@PathVariable Long id, HttpServletRequest request) {
        UserVO user = getLoginUser(request);
        return ResultUtils.success(messageService.markRead(id, user.getId()));
    }
}
