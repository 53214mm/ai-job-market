package com.li.ai_job_market.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Hello测试控制器 */
@RestController
public class hello {
    // 返回Hello World测试响应
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
