package com.itheima.consultant.dto;

import dev.langchain4j.data.document.Metadata;

import java.util.Map;

public class CodeChunk {
    private String content;
    private Map<String, Object> metadata;

    public CodeChunk(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}