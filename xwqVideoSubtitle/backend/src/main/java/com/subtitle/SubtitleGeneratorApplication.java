package com.subtitle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.subtitle.mapper")
public class SubtitleGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubtitleGeneratorApplication.class, args);
    }
}