package com.yida.safety;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yida.safety.dao")
public class SafetyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafetyApplication.class, args);
    }

}
