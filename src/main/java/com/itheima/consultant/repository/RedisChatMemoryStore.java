package com.itheima.consultant.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {
    //注入redis
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<ChatMessage> getMessages(Object MemoryId) {
        //获取对话消息
        String message = redisTemplate.opsForValue().get(MemoryId);
        //还需要转换成list
        List<ChatMessage> chatMessage = ChatMessageDeserializer.messagesFromJson(message);
        return chatMessage;
    }

    @Override
    public void updateMessages(Object MemoryId, List<ChatMessage> list) {
        //更新会话消息
        //1.list转换为json
        String json = ChatMessageSerializer.messagesToJson(list);
        //2.json存到redis
        redisTemplate.opsForValue().set(MemoryId.toString(), json, Duration.ofDays(1));
    }

    @Override
    public void deleteMessages(Object MemoryId) {
        redisTemplate.delete(MemoryId.toString());

    }
}
