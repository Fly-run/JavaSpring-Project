package com.stewie.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城启动类。
 * @MapperScan 扫描 Mapper 接口,生成代理实现并注册到 Spring 容器。
 */
@SpringBootApplication
@MapperScan("com.stewie.mall.mapper")
public class MallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallApplication.class, args);
    }
}
