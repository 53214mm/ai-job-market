package com.li.ai_job_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.li.ai_job_market.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /** 将某个会话中所有未读消息批量标记为已读 */
    @org.apache.ibatis.annotations.Update(
        "UPDATE message SET is_read = 1 WHERE receiver_id = #{userId} AND sender_id = #{peerId} AND is_read = 0")
    int markAllReadFromPeer(@org.apache.ibatis.annotations.Param("userId") Long userId,
                            @org.apache.ibatis.annotations.Param("peerId") Long peerId);

    /** 将该用户所有未读私信批量标记为已读 */
    @org.apache.ibatis.annotations.Update(
        "UPDATE message SET is_read = 1 WHERE receiver_id = #{userId} AND is_read = 0")
    int markAllRead(@org.apache.ibatis.annotations.Param("userId") Long userId);

    /** 删除当前用户与指定对端之间的所有私信（仅删除当前用户参与的记录） */
    @org.apache.ibatis.annotations.Delete(
        "DELETE FROM message WHERE (sender_id = #{userId} AND receiver_id = #{peerId}) OR (sender_id = #{peerId} AND receiver_id = #{userId})")
    int deleteConversation(@org.apache.ibatis.annotations.Param("userId") Long userId,
                           @org.apache.ibatis.annotations.Param("peerId") Long peerId);
}
