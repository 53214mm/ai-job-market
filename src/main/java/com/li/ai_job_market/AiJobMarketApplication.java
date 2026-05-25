package com.li.ai_job_market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.li.ai_job_market.mapper")
public class AiJobMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiJobMarketApplication.class, args);
    }

}
