package com.itheima.consultant.controller;

import com.itheima.consultant.aiservice.ConsultantService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ChatController {
//    @Autowired
//    private OpenAiChatModel model;
//
//    @RequestMapping("/chat")
//    public String chat(String message) {
//        String result= model.chat(message);
//        return result;
//    }
    @Autowired
    private ConsultantService consultantService;
    @RequestMapping("/chat")
    public String chat(String message) {
        String result= consultantService.chat(message);
        return result;
    }
}
