package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.Notification;
import com.li.ai_job_market.model.vo.NotificationVO;

public interface NotificationService extends IService<Notification> {

    void create(Long userId, String type, String title, String content, Long relatedId);

    Page<NotificationVO> listMyNotifications(Long userId, int current, int size);

    long unreadCount(Long userId);

    boolean markRead(Long id, Long userId);

    boolean markAllRead(Long userId);

    boolean delete(Long id, Long userId);
}
