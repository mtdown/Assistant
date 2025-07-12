package com.itheima.consultant.config;

import com.itheima.consultant.aiservice.ConsultantService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Autowired
    private OpenAiChatModel model;
//    @Bean
//    public ConsultantService consultantService() {
//        ConsultantService consultantService = AiServices.builder(ConsultantService.class)
//                .chatModel(model)
//                .build();
//        return consultantService;
//    }
}
