package com.itheima.consultant.config; // 或者其他您选择的包

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "rag.ingestion.enabled", havingValue = "true")
public class DataIngestionRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestionRunner.class);

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore; // 直接注入 EmbeddingStore

    @Autowired
    private EmbeddingModel embeddingModel;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting RAG data ingestion... 'rag.ingestion.enabled' is true.");

        // 1. 从本地博客文章文件夹加载所有 .md 文件
        logger.info("Loading documents from C:\\blog\\source\\_posts");
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("C:\\blog\\source\\_posts");
        logger.info("Loaded {} documents", documents.size());

        // 2. 创建 Ingestor 并注入数据
        DocumentSplitter recursive = DocumentSplitters.recursive(500, 100);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive)
                .build();

        ingestor.ingest(documents);

        logger.info("RAG data ingestion complete.");
    }
}