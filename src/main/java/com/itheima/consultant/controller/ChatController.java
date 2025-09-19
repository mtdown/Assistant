package com.itheima.consultant.controller;

import com.itheima.consultant.aiservice.ConsultantService;
import com.itheima.consultant.dto.ChatRequest;
import com.itheima.consultant.service.RagIngestionService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.data.embedding.Embedding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private RagIngestionService ragIngestionService;

    // --- 【改造点1】我们不再注入完整的AiService，而是注入构建它所需的"原材料" ---
    @Autowired
    private StreamingChatModel streamingChatModel;
    @Autowired
    private ChatMemoryProvider chatMemoryProvider;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;
    @Autowired
    private EmbeddingModel embeddingModel;

    @PostMapping("/upload-project")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "上传的文件不能为空"));
        }
        try {
            String sessionId = UUID.randomUUID().toString();
            ragIngestionService.ingest(file, sessionId);
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (IOException e) {
            logger.error("处理上传文件时发生IO错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "处理文件时发生内部错误"));
        }
    }

    /**
     * 【改造点2】改造后的聊天端点 - 使用自定义过滤逻辑替代 Filters
     * @param request 包含 message 和 sessionId 的请求体
     * @return AI的流式响应
     */
    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String message = request.getMessage();
        String sessionId = request.getSessionId();
        logger.info("收到来自会话 '{}' 的消息: {}", sessionId, message);

        // 【核心】步骤1：创建智能内容检索器，优先当前会话内容，酌情添加其他相关内容
        ContentRetriever contentRetriever = new ContentRetriever() {
            @Override
            public List<dev.langchain4j.rag.content.Content> retrieve(dev.langchain4j.rag.query.Query query) {
                // 将查询文本转换为向量
                Embedding queryEmbedding = embeddingModel.embed(query.text()).content();

                // 搜索相似的文档片段 - 获取更多结果用于智能筛选
                EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(20) // 获取足够多的候选结果
                        .minScore(0.6) // 设置最低相似度阈值
                        .build();

                EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

                // 分离当前会话的内容和其他内容
                List<EmbeddingMatch<TextSegment>> currentSessionMatches = searchResult.matches().stream()
                        .filter(match -> {
                            TextSegment segment = match.embedded();
                            return segment.metadata() != null &&
                                    sessionId.equals(segment.metadata().getString("sessionId"));
                        })
                        .collect(Collectors.toList());

                List<EmbeddingMatch<TextSegment>> otherMatches = searchResult.matches().stream()
                        .filter(match -> {
                            TextSegment segment = match.embedded();
                            return segment.metadata() == null ||
                                    !sessionId.equals(segment.metadata().getString("sessionId"));
                        })
                        .collect(Collectors.toList());

                // 智能组合结果：优先当前会话，酌情添加其他高相关度内容
                List<EmbeddingMatch<TextSegment>> finalMatches = new java.util.ArrayList<>();

                // 1. 优先加入当前会话的所有相关内容（最多取5个）
                finalMatches.addAll(currentSessionMatches.stream()
                        .limit(5)
                        .collect(Collectors.toList()));

                // 2. 如果当前会话内容不足3个，或者其他内容相似度很高（>0.85），则酌情添加
                int currentSessionCount = finalMatches.size();
                if (currentSessionCount < 3) {
                    // 当前会话内容不足，补充其他高质量内容
                    finalMatches.addAll(otherMatches.stream()
                            .filter(match -> match.score() > 0.8) // 高相似度阈值
                            .limit(3 - currentSessionCount)
                            .collect(Collectors.toList()));
                } else {
                    // 当前会话内容充足，只添加极高相似度的其他内容
                    finalMatches.addAll(otherMatches.stream()
                            .filter(match -> match.score() > 0.9) // 极高相似度阈值
                            .limit(2) // 最多添加2个
                            .collect(Collectors.toList()));
                }

                // 确保最终结果不超过7个
                List<EmbeddingMatch<TextSegment>> limitedMatches = finalMatches.stream()
                        .limit(7)
                        .collect(Collectors.toList());

                logger.info("会话 {} 检索到内容: 当前会话{}个, 其他内容{}个, 总计{}个",
                        sessionId, currentSessionCount,
                        limitedMatches.size() - currentSessionCount,
                        limitedMatches.size());

                // 将匹配结果转换为 Content 对象
                return limitedMatches.stream()
                        .map(match -> dev.langchain4j.rag.content.Content.from(match.embedded().text()))
                        .collect(Collectors.toList());
            }
        };

        // 【核心】步骤2：使用上面的自定义内容检索器，动态构建一个本次请求专用的AI服务
        ConsultantService service = AiServices.builder(ConsultantService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(contentRetriever) // 注入自定义检索器
                .build();

        // 步骤3：使用这个专属服务来处理对话
        // @MemoryId注解会告诉服务，使用sessionId作为会话记忆的唯一标识
        return service.chat(sessionId, message);
    }
}