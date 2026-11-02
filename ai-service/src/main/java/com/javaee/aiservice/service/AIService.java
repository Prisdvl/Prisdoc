package com.javaee.aiservice.service;

import com.javaee.aiservice.agent.ChatService;
import com.javaee.aiservice.agent.PromptEngineeringService;
import com.javaee.aiservice.dto.KeywordExtractDTO;
import com.javaee.aiservice.dto.TextAnalyzeDTO;
import com.javaee.aiservice.dto.TextSummarizeDTO;
import com.javaee.aiservice.vo.KeywordExtractVO;
import com.javaee.aiservice.vo.KeywordVO;
import com.javaee.aiservice.vo.TextAnalyzeVO;
import com.javaee.aiservice.vo.TextSummarizeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 服务，提供文档摘要、文本分析、关键词提取与文档纠错能力。
 *
 * <p>摘要与关键词提取的 Prompt 由 PromptEngineeringService 构建，经 ChatService 调用大模型，
 * 并支持指定模型代码；文本分析与纠错为本地规则实现。</p>
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private PromptEngineeringService promptEngineeringService;

    /**
     * 使用默认模型生成文档摘要。
     *
     * @param dto 请求参数，包含原文与摘要长度上限
     * @return 摘要结果，含摘要文本与压缩比
     */
    public TextSummarizeVO summarize(TextSummarizeDTO dto) {
        return summarize(dto, null);
    }

    /**
     * 使用指定模型生成文档摘要。
     *
     * @param dto 请求参数，包含原文与摘要长度上限
     * @param model 模型代码，为空时使用默认模型
     * @return 摘要结果，含摘要文本与压缩比
     */
    public TextSummarizeVO summarize(TextSummarizeDTO dto, String model) {
        log.info("开始文档摘要，文本长度={}, 模型={}", dto.getContent().length(), model);

        String summary = chatService.callChatApiWithModelCode(
            promptEngineeringService.createSummarizePrompt(dto.getContent(), dto.getMaxLength()),
            model
        );

        TextSummarizeVO vo = new TextSummarizeVO();
        vo.setSummary(summary);
        vo.setOriginalLength(dto.getContent().length());
        vo.setSummaryLength(summary.length());
        vo.setCompressionRatio((double) summary.length() / dto.getContent().length());

        log.info("文档摘要完成");
        return vo;
    }

    /**
     * 统计文档的字符构成，包括中英文字符、数字、空白、标点与行数。
     *
     * @param dto 请求参数，包含待分析文本
     * @return 文本分析结果
     */
    public TextAnalyzeVO analyze(TextAnalyzeDTO dto) {
        log.info("开始文档分析");

        String content = dto.getContent();
        
        TextAnalyzeVO vo = new TextAnalyzeVO();
        vo.setTotalCharacters(content.length());
        
        int chineseCount = 0, englishCount = 0, digitCount = 0, spaceCount = 0, punctuationCount = 0;
        
        for (char c : content.toCharArray()) {
            if (Character.isIdeographic(c)) {
                chineseCount++;
            } else if (Character.isLetter(c)) {
                englishCount++;
            } else if (Character.isDigit(c)) {
                digitCount++;
            } else if (Character.isWhitespace(c)) {
                spaceCount++;
            } else if (isPunctuation(c)) {
                punctuationCount++;
            }
        }
        
        vo.setChineseCharacters(chineseCount);
        vo.setEnglishCharacters(englishCount);
        vo.setDigits(digitCount);
        vo.setSpaces(spaceCount);
        vo.setPunctuations(punctuationCount);
        vo.setLines(content.split("\n").length);
        
        log.info("文档分析完成");
        return vo;
    }

    /**
     * 判断字符是否为中英文标点符号。
     */
    private boolean isPunctuation(char c) {
        return "，。！？；：、\"\"''（）{}[]<>《》·".indexOf(c) >= 0 ||
                ",.!?;:\"'(){}[]<>".indexOf(c) >= 0;
    }

    /**
     * 使用默认模型提取关键词。
     *
     * @param dto 请求参数，包含原文与关键词数量
     * @return 关键词提取结果
     */
    public KeywordExtractVO extractKeywords(KeywordExtractDTO dto) {
        return extractKeywords(dto, null);
    }

    /**
     * 使用指定模型提取关键词。
     *
     * @param dto 请求参数，包含原文与关键词数量
     * @param model 模型代码，为空时使用默认模型
     * @return 关键词提取结果
     */
    public KeywordExtractVO extractKeywords(KeywordExtractDTO dto, String model) {
        log.info("开始关键词提取，模型={}", model);

        String keywordsStr = chatService.callChatApiWithModelCode(
            promptEngineeringService.createKeywordExtractPrompt(dto.getContent(), dto.getCount()),
            model
        );

        List<KeywordVO> keywords = Arrays.stream(keywordsStr.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(word -> new KeywordVO(word, 1.0, "keyword"))
            .collect(Collectors.toList());

        KeywordExtractVO vo = new KeywordExtractVO();
        vo.setKeywords(keywords);
        vo.setTotalCount(keywords.size());

        log.info("关键词提取完成，共{}个关键词", keywords.size());
        return vo;
    }

    /**
     * 对文档执行纠错并返回文本分析结果。
     *
     * @param dto 请求参数，包含待纠错文本
     * @return 文本分析结果
     */
    public TextAnalyzeVO correct(TextAnalyzeDTO dto) {
        log.info("开始文档纠错");
        return analyze(dto);
    }
}