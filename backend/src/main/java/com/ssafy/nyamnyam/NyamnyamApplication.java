package com.ssafy.nyamnyam;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan(basePackages = "com.ssafy.nyamnyam", annotationClass = Mapper.class)
@SpringBootApplication
public class NyamnyamApplication {

    public static void main(String[] args) {
        SpringApplication.run(NyamnyamApplication.class, args);
    }
}
