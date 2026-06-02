package com.li.ai_job_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.li.ai_job_market.model.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /** 根据关联ID（如消息ID）将对应通知标记为已读 */
    @org.apache.ibatis.annotations.Update(
        "UPDATE notification SET is_read = 1 WHERE related_id = #{relatedId} AND is_read = 0")
    int markReadByRelatedId(@org.apache.ibatis.annotations.Param("relatedId") Long relatedId);
}
