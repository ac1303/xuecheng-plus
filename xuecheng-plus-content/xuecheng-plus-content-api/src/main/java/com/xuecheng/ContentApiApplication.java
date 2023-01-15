package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 小王的饭饭
 * @create 2023/1/15 20:06
 */
@EnableSwagger2Doc
@SpringBootApplication
public class ContentApiApplication {
//    测试测试
    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class,args);
    }
}
