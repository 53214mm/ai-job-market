package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.exception.ErrorCode;
import com.li.ai_job_market.exception.ThrowUtils;
import com.li.ai_job_market.mapper.MessageMapper;
import com.li.ai_job_market.mapper.UserMapper;
import com.li.ai_job_market.model.entity.Message;
import com.li.ai_job_market.model.entity.User;
import com.li.ai_job_market.model.vo.MessageVO;
import com.li.ai_job_market.service.MessageService;
import com.li.ai_job_market.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/** 私信消息服务实现，支持WebSocket实时推送和自动通知提醒 */
@Slf4j
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    @Resource private UserMapper userMapper;
    @Resource private NotificationService notificationService;
    @Resource private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO send(Long senderId, Long receiverId, Long applicationId, String content) {
        User receiver = userMapper.selectById(receiverId);
        ThrowUtils.throwIf(receiver == null, ErrorCode.NOT_FOUND_ERROR, "接收方不存在");

        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setApplicationId(applicationId);
        msg.setContent(content);
        msg.setIsRead(0);
        this.save(msg);

        MessageVO vo = toVO(msg);

        // 自动创建通知提醒接收方
        try {
            long notifCount = notificationService.unreadCount(receiverId);
            notificationService.create(receiverId, "MESSAGE", "您收到一条新私信",
                    content.length() > 50 ? content.substring(0, 50) + "..." : content, msg.getId());
            pushUnreadCount(receiverId);
        } catch (Exception e) {
            log.warn("创建私信通知失败: {}", e.getMessage());
        }

        // WebSocket 实时推送给收发双方
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId), "/queue/messages", vo);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(senderId), "/queue/messages", vo);
        } catch (Exception e) {
            log.warn("WebSocket 推送失败: {}", e.getMessage());
        }

        log.info("私信已发送: senderId={}, receiverId={}, msgId={}", senderId, receiverId, msg.getId());
        return vo;
    }

    private void pushUnreadCount(Long userId) {
        try {
            long total = notificationService.unreadCount(userId) + unreadCount(userId);
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId), "/queue/unread-count", Map.of("count", total));
        } catch (Exception e) {
            // ignore push failure
        }
    }

    @Override
    public Page<MessageVO> getConversation(Long userId, Long peerId, int current, int size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.and(wr -> wr
                .eq(Message::getSenderId, userId).eq(Message::getReceiverId, peerId)
                .or()
                .eq(Message::getSenderId, peerId).eq(Message::getReceiverId, userId)
        ).orderByAsc(Message::getCreatedAt);
        Page<Message> page = this.page(new Page<>(current, size), w);
        Page<MessageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public List<MessageVO> listConversations(Long userId) {
        // 找出所有与我有关的消息中最新的对方
        List<Map<String, Object>> raw = this.getBaseMapper().selectMaps(
                new LambdaQueryWrapper<Message>()
                        .select(Message::getSenderId, Message::getReceiverId)
                        .and(w -> w.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId))
        );
        if (raw.isEmpty()) return List.of();

        // 收集所有 peerId
        Set<Long> peerIds = new HashSet<>();
        for (Map<String, Object> row : raw) {
            Long sid = (Long) row.get("sender_id");
            Long rid = (Long) row.get("receiver_id");
            if (!sid.equals(userId)) peerIds.add(sid);
            if (!rid.equals(userId)) peerIds.add(rid);
        }

        // 对每个 peer 取最新一条消息
        List<MessageVO> result = new ArrayList<>();
        for (Long peerId : peerIds) {
            LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
            w.and(wr -> wr
                    .eq(Message::getSenderId, userId).eq(Message::getReceiverId, peerId)
                    .or()
                    .eq(Message::getSenderId, peerId).eq(Message::getReceiverId, userId)
            ).orderByDesc(Message::getCreatedAt).last("LIMIT 1");
            Message latest = this.getOne(w);
            if (latest != null) result.add(toVO(latest));
        }
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }

    @Override
    public long unreadCount(Long userId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0);
        return this.count(w);
    }

    @Override
    public boolean markRead(Long id, Long userId) {
        Message msg = this.getById(id);
        ThrowUtils.throwIf(msg == null || !msg.getReceiverId().equals(userId),
                ErrorCode.NO_AUTH_ERROR, "无权操作此消息");
        msg.setIsRead(1);
        return this.updateById(msg);
    }

    private MessageVO toVO(Message msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setSenderId(msg.getSenderId());
        vo.setReceiverId(msg.getReceiverId());
        vo.setApplicationId(msg.getApplicationId());
        vo.setContent(msg.getContent());
        vo.setIsRead(msg.getIsRead());
        vo.setCreatedAt(msg.getCreatedAt());

        User sender = userMapper.selectById(msg.getSenderId());
        if (sender != null) vo.setSenderName(sender.getNickname());
        User receiver = userMapper.selectById(msg.getReceiverId());
        if (receiver != null) vo.setReceiverName(receiver.getNickname());
        return vo;
    }
}
