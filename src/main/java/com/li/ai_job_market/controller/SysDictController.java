package com.li.ai_job_market.controller;

import com.li.ai_job_market.common.BaseResponse;
import com.li.ai_job_market.common.ResultUtils;
import com.li.ai_job_market.service.SysDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dict")
public class SysDictController {

    @Resource private SysDictService sysDictService;

    @GetMapping("/{type}")
    public BaseResponse<Map<String, String>> getDict(@PathVariable String type) {
        return ResultUtils.success(sysDictService.mapByType(type));
    }

    @GetMapping("/{type}/{key}")
    public BaseResponse<String> getValue(@PathVariable String type, @PathVariable String key) {
        return ResultUtils.success(sysDictService.getValue(type, key));
    }
}
