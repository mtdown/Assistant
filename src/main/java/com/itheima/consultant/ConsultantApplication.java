package com.itheima.consultant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsultantApplication {

    public static void main(String[] args) {
//run方法会创建一个spring容器
        SpringApplication.run(ConsultantApplication.class, args);
    }

}

