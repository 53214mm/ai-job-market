package com.li.ai_job_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.li.ai_job_market.mapper.SysDictMapper;
import com.li.ai_job_market.model.entity.SysDict;
import com.li.ai_job_market.service.SysDictService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict>
        implements SysDictService {

    @Override
    public List<SysDict> listByType(String dictType) {
        return this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getSortOrder));
    }

    @Override
    public Map<String, String> mapByType(String dictType) {
        Map<String, String> map = new LinkedHashMap<>();
        for (SysDict d : listByType(dictType)) {
            map.put(d.getDictKey(), d.getDictValue());
        }
        return map;
    }

    @Override
    public String getValue(String dictType, String dictKey) {
        SysDict dict = this.getOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getDictKey, dictKey));
        return dict != null ? dict.getDictValue() : null;
    }
}
