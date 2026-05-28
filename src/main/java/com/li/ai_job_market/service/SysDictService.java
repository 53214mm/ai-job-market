package com.li.ai_job_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.li.ai_job_market.model.entity.SysDict;

import java.util.List;
import java.util.Map;

public interface SysDictService extends IService<SysDict> {

    List<SysDict> listByType(String dictType);

    Map<String, String> mapByType(String dictType);

    String getValue(String dictType, String dictKey);
}
