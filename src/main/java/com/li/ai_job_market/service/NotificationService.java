package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.Notification;
import com.li.ai_job_market.model.vo.NotificationVO;

/** 系统通知服务接口，提供通知创建、查询、已读标记和删除功能 */
public interface NotificationService extends IService<Notification> {

    void create(Long userId, String type, String title, String content, Long relatedId);

    Page<NotificationVO> listMyNotifications(Long userId, int current, int size);

    long unreadCount(Long userId);

    boolean markRead(Long id, Long userId);

    boolean markAllRead(Long userId);

    boolean delete(Long id, Long userId);
}
