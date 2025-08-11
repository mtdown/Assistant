package com.itheima.consultant.config;

import com.itheima.consultant.aiservice.ConsultantService;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonConfig {
//    @Autowired
//    private OpenAiChatModel model;
//    @Bean
//    public ConsultantService consultantService(){
//        ConsultantService cs= AiServices.builder(ConsultantService.class).chatModel(model).build();
//        return cs;
//    };

//    @Bean
//    public ChatMemory chatMemory() {//构建会话记忆对象
//        MessageWindowChatMemory memory = MessageWindowChatMemory
//                .builder()
//                .maxMessages(20)
//                .build();
//        return  memory;
//    }
    @Autowired
    private ChatMemoryStore chatMemoryStore;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {//构建会话记忆对象
        ChatMemoryProvider chatMemoryProvider = new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build();
            }
        };
        return chatMemoryProvider;
    }
    @Bean
//    构建向量数据库操作对象
    public EmbeddingStore Store() {
        //1加载文件进内存
//        List<Document> content = ClassPathDocumentLoader.loadDocuments("content");//文档分割器使用位置
        List<Document> content = FileSystemDocumentLoader.loadDocuments("C:\\Users\\origin\\IdeaProjects\\consultant\\src\\main\\resources\\pdfcontent",new ApachePdfBoxDocumentParser());
        //2构建向量数据库操作对象
//        InMemoryEmbeddingStore embeddingStore = new InMemoryEmbeddingStore();

        //3.追加一个文档分割器，以方便将长文本进行切分
        DocumentSplitter recursive = DocumentSplitters.recursive(500,100);
        //切割文档存到向量数据库,构建embeddingstore对象
        EmbeddingStoreIngestor embeddingStoreIngestor=EmbeddingStoreIngestor.builder()
                .embeddingStore(redisEmbeddingStore)//分词用
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive)//分割文档用
                .build();
        embeddingStoreIngestor.ingest(content);

//        return embeddingStore;
        return redisEmbeddingStore;
    }
    @Bean
    //构建向量数据检索对象
    public ContentRetriever contentRetriever() {//有意思，这里EmbeddingStore Store如果使用了默认的embeddingStore 会导致无法读取文件内容
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(redisEmbeddingStore)
                .embeddingModel(embeddingModel)
                .minScore(0.5)
                .maxResults(3)
                .build();
    }
}

