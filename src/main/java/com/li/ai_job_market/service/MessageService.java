package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.Message;
import com.li.ai_job_market.model.vo.MessageVO;

import java.util.List;

/** 私信消息服务接口，提供用户间私信发送、会话管理和未读统计功能 */
public interface MessageService extends IService<Message> {

    MessageVO send(Long senderId, Long receiverId, Long applicationId, String content);

    Page<MessageVO> getConversation(Long userId, Long peerId, int current, int size);

    List<MessageVO> listConversations(Long userId);

    long unreadCount(Long userId);

    boolean markRead(Long id, Long userId);

    /** 批量标记某个会话中的所有未读消息为已读 */
    int markAllReadFromPeer(Long userId, Long peerId);

    /** 将该用户所有未读私信批量标记为已读 */
    int markAllRead(Long userId);

    /** 删除当前用户与指定对端之间的整个私聊记录 */
    int deleteConversation(Long userId, Long peerId);
}
