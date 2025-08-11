package com.itheima.consultant.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

//起接口作用
@AiService(//有意思，如果使用了这个，那么就不需要再COMMONCONFIG里面用bean自己写一个注入
        wiringMode = AiServiceWiringMode.EXPLICIT //手动装配，默认自动装配，在IOC容器找model对象
//        ,chatModel = "openAiChatModel"//指定模型
        ,streamingChatModel = "openAiStreamingChatModel"//这里的streamingChatModel是一个定义的类型，后面那个“”是自定义的名字
//        ,chatMemory = "chatMemory"//配置会话记忆
        ,chatMemoryProvider = "chatMemoryProvider"//配置会话记忆
        ,contentRetriever = "contentRetriever"//配置向量数据库检索对象
        ,tools = "reservationTool"
)
//@AiService
public interface ConsultantService {
    @SystemMessage(fromResource = "system.txt")
//    public String chat(String message);
//    @UserMessage("你是摄夜的助手夜夜{{it}}")//用户消息，而不是系统消息
//    @UserMessage("你是摄夜的助手hpyer刚大木x3最终改造实验型无双作战兵器{{msg}}")//用户消息，而不是系统消息
//    public Flux<String> chat(@V("msg") String message);
    public Flux<String> chat(
            @MemoryId String memoryId,
            @UserMessage String message);
}
