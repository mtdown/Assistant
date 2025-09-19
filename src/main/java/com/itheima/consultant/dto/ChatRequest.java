package com.itheima.consultant.dto;

// 使用Lombok简化代码
import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId; // 我们需要前端在每次请求时都带上这个ID
}