//package com.itheima.consultant.controller;
//
//import com.itheima.consultant.aiservice.ConsultantService;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Flux;
//
//@RestController
//
//// 同时允许本地开发环境和线上生产环境的跨域请求
//@CrossOrigin({"http://localhost:4000", "https://mtdown.top"})
//public class ChatController {
//    @Autowired
//    private ConsultantService consultantService;
//
//    //这个接口将要产出的是HTML文本
////    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
//    //当我们需要作为一个小助手时，使用这个
//    @RequestMapping(value = "/chat", produces = "text/event-stream;charset=utf-8")
////    public String chat(String message){
////        String result = consultantService.chat(message);
////        return result;
////    }
//    public Flux<String> chat(String memoryId,String message){
//        Flux<String> result = consultantService.chat(memoryId,message);
//        return result;
//    }
//
//}

package com.itheima.consultant.controller;

import com.itheima.consultant.aiservice.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Optional;

@RestController
// 允许所有需要的来源
@CrossOrigin({"http://localhost:4000", "https://mtdown.top", "https://model.mtdown.top"})
public class ChatController {

    @Autowired
    private ConsultantService consultantService;

    /**
     * 统一的/chat接口，能同时处理流式和非流式请求
     * @param memoryId 会话ID
     * @param message 用户消息
     * @param acceptHeader Spring会自动注入请求中的"Accept"头信息
     * @return 根据Accept头的不同，返回Flux<String>或String
     */
    @RequestMapping(value = "/chat")
    public Object unifiedChat(String memoryId, String message, @RequestHeader("Accept") Optional<String> acceptHeader) {

        // 检查Accept头是否明确要求 event-stream
        boolean isStreamRequest = acceptHeader.isPresent() && acceptHeader.get().contains(MediaType.TEXT_EVENT_STREAM_VALUE);

        if (isStreamRequest) {
            // 如果是流式请求 (来自您的AI助手)，返回Flux<String>，Spring会自动处理
            System.out.println("Handling as a STREAM request."); // 方便您在后端日志中观察
            return consultantService.chat(memoryId, message);
        } else {
            // 如果是其他所有请求 (例如您的大模型项目页面)，则阻塞并返回完整字符串
            System.out.println("Handling as a FULL TEXT/HTML request."); // 方便您在后端日志中观察
            String fullResponse = consultantService.chat(memoryId, message)
                    .collectList()
                    .block()
                    .stream()
                    .reduce("", String::concat);
            return fullResponse;
        }
    }
}