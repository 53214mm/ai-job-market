package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.MessageMapper;
import com.li.ai_job_market.mapper.NotificationMapper;
import com.li.ai_job_market.model.entity.Message;
import com.li.ai_job_market.model.entity.Notification;
import com.li.ai_job_market.model.vo.NotificationVO;
import com.li.ai_job_market.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/** 系统通知服务实现，提供通知的创建、查询、批量已读和WebSocket实时推送未读数 */
@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Resource private MessageMapper messageMapper;
    @Resource private SimpMessagingTemplate messagingTemplate;

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "APPLICATION_UPDATE", "投递更新",
            "INTERVIEW_INVITE", "面试邀请",
            "MESSAGE", "新消息",
            "SYSTEM", "系统通知"
    );

    private void pushUnreadCount(Long userId) {
        try {
            long notifCount = unreadCount(userId);
            LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
            w.eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0);
            long msgCount = messageMapper.selectCount(w);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId), "/queue/unread-count",
                    Map.of("count", notifCount + msgCount));
        } catch (Exception ignored) {}
    }

    @Override
    public void create(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        this.save(n);
        pushUnreadCount(userId);
        log.info("通知已创建: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    public Page<NotificationVO> listMyNotifications(Long userId, int current, int size) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).orderByDesc(Notification::getCreatedAt);
        Page<Notification> page = this.page(new Page<>(current, size), w);
        Page<NotificationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public long unreadCount(Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return this.count(w);
    }

    @Override
    public boolean markRead(Long id, Long userId) {
        Notification n = this.getById(id);
        ThrowUtils.throwIf(n == null || !n.getUserId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "无权操作此通知");
        n.setIsRead(1);
        boolean ok = this.updateById(n);
        pushUnreadCount(userId);
        return ok;
    }

    @Override
    public boolean markAllRead(Long userId) {
        boolean ok = lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .update();
        pushUnreadCount(userId);
        return ok;
    }

    @Override
    public boolean delete(Long id, Long userId) {
        Notification n = this.getById(id);
        ThrowUtils.throwIf(n == null || !n.getUserId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "无权删除此通知");
        boolean ok = this.removeById(id);
        pushUnreadCount(userId);
        return ok;
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setRelatedId(n.getRelatedId());
        vo.setIsRead(n.getIsRead());
        vo.setCreatedAt(n.getCreatedAt());
        vo.setTypeLabel(TYPE_LABELS.getOrDefault(n.getType(), n.getType()));
        return vo;
    }
}
