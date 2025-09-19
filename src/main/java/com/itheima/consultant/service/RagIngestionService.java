package com.itheima.consultant.service;

import com.itheima.consultant.dto.CodeChunk;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
@Service
public class RagIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(RagIngestionService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".java", ".js", ".py", ".md", ".txt", ".html", ".css", ".xml", ".yml", ".properties");

    // 【已移除】删除了不安全的 HttpSession 注入

    public void ingest(MultipartFile file, String sessionId) throws IOException {
        logger.info("开始为会话 '{}' 处理文件 '{}'", sessionId, file.getOriginalFilename());
        List<CodeChunk> chunks = unzipAndParse(file.getInputStream(), sessionId);
        embedAndStore(chunks);
        logger.info("会话 '{}' 的文件处理和知识注入完成", sessionId);
    }


    private List<CodeChunk> unzipAndParse(InputStream inputStream, String sessionId) throws IOException {
        // 1. 先将输入流完整读入内存中的 byte 数组
        // 这样我们就可以重复地、安全地读取它
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        inputStream.transferTo(baos);
        byte[] fileBytes = baos.toByteArray();

        // 2. 优先尝试使用标准的 UTF-8 编码
        try {
            logger.info("尝试使用 UTF-8 编码解压文件...");
            return tryUnzip(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8, sessionId);
        } catch (ZipException e) {
            logger.warn("使用 UTF-8 编码解压失败: {}. 正在尝试使用 GBK 编码...", e.getMessage());

            // 3. 如果 UTF-8 失败，再尝试使用 GBK 编码
            try {
                return tryUnzip(new ByteArrayInputStream(fileBytes), Charset.forName("GBK"), sessionId);
            } catch (IOException finalException) {
                // 如果两种编码都失败了，就抛出最终的异常
                logger.error("使用 UTF-8 和 GBK 两种编码尝试解压均失败。");
                throw finalException;
            }
        }
    }

    private List<CodeChunk> tryUnzip(InputStream inputStream, Charset charset, String sessionId) throws IOException {
        List<CodeChunk> chunks = new ArrayList<>();
        // 在这里传入指定的编码
        try (ZipInputStream zis = new ZipInputStream(inputStream, charset)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String fileName = zipEntry.getName();

                // ... (这里是和你原来一样的文件处理逻辑)
                if (isSupportedFile(fileName)) {
                    String content = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    if (content.trim().isEmpty()) {
                        continue;
                    }
                    chunks.addAll(chunkContent(content, fileName, sessionId));
                }
            }
        }
        logger.info("成功使用 {} 编码解压文件。", charset.name());
        return chunks;
    }

    private void embedAndStore(List<CodeChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            logger.info("没有要注入的代码块，流程结束。");
            return;
        }
        List<TextSegment> segments = new ArrayList<>();
        for (CodeChunk chunk : chunks) {
            segments.add(TextSegment.from(chunk.getContent(), chunk.getMetadata()));
        }
        logger.info("正在对 {} 个文本片段进行向量化...", segments.size());
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        logger.info("向量化完成。");
        List<String> ids = embeddingStore.addAll(embeddings, segments);
        logger.info("成功注入了 {} 个代码块到向量数据库，生成的ID数量为: {}", segments.size(), ids.size());
    }

    private List<CodeChunk> chunkContent(String content, String filePath, String sessionId) {
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);
        List<TextSegment> segments = splitter.split(Document.from(content));
        List<CodeChunk> codeChunks = new ArrayList<>();
        for (TextSegment segment : segments) {
            Map<String, Object> metadataMap = new HashMap<>();
            metadataMap.put("source", filePath);
            metadataMap.put("sessionId", sessionId);
            Metadata metadata = new Metadata(metadataMap);
            codeChunks.add(new CodeChunk(segment.text(), metadata));
        }
        logger.info("为文件 '{}' 创建了 {} 个代码块", filePath, codeChunks.size());
        return codeChunks;
    }

    private boolean isSupportedFile(String fileName) {
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}