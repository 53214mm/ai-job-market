package com.li.ai_job_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.li.ai_job_market.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
