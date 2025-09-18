package com.itheima.consultant.dto;

import dev.langchain4j.data.document.Metadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // @Data注解会自动生成Getters, Setters, toString, equals, hashCode等方法
@NoArgsConstructor // 生成一个无参构造函数
@AllArgsConstructor // 生成一个包含所有字段的构造函数
public class CodeChunk {
    private String content;
    private Metadata metadata;
}