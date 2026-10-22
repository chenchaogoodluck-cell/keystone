package com.keystone.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Keystone 应用入口
 *
 * @author Keystone
 */
@SpringBootApplication
@MapperScan("com.keystone.admin.mapper")
@EnableAsync
public class KeystoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeystoneApplication.class, args);
    }
}