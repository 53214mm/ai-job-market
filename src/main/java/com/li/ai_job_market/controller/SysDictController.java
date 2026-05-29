package com.li.ai_job_market.controller;

import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.service.SysDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统字典控制器 —— 提供字典数据的按类型和键值查询功能
 */
@Slf4j
@RestController
@RequestMapping("/dict")
public class SysDictController {

    @Resource private SysDictService sysDictService;

    // 根据字典类型获取所有键值对
    @GetMapping("/{type}")
    public BaseResponse<Map<String, String>> getDict(@PathVariable String type) {
        return ResultUtils.success(sysDictService.mapByType(type));
    }

    // 根据字典类型和键获取单个值
    @GetMapping("/{type}/{key}")
    public BaseResponse<String> getValue(@PathVariable String type, @PathVariable String key) {
        return ResultUtils.success(sysDictService.getValue(type, key));
    }
}
