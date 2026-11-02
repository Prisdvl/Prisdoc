package com.javaee.aiservice.config;

import com.javaee.aiservice.rag.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文档分段策略装配，向容器注册默认策略与各分段实现。
 *
 * <p>默认使用章节分段；固定长度、章节与语义三种实现按预设参数注册为独立 Bean。</p>
 */
@Configuration
public class SegmentConfig {

    @Bean
    public DocumentSegmenter.StrategyType defaultSegmentStrategy() {
        return DocumentSegmenter.StrategyType.CHAPTER;
    }

    @Bean
    public FixedLengthSegmentStrategy fixedLengthSegmentStrategy() {
        return new FixedLengthSegmentStrategy(500, 50);
    }

    @Bean
    public ChapterSegmentStrategy chapterSegmentStrategy() {
        return new ChapterSegmentStrategy(100, 5000);
    }

    @Bean
    public SemanticSegmentStrategy semanticSegmentStrategy() {
        return new SemanticSegmentStrategy(300, 100, 75);
    }
}