package com.javaee.documentservice.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档版本展示对象，向前端返回单个历史版本的内容与变更信息。
 */
@Data
public class DocumentVersionVO {

    /** 版本记录 ID */
    private String id;

    /** 所属文档 ID */
    private String documentId;

    /** 版本号 */
    private Integer versionNumber;

    /** 该版本的文档标题 */
    private String title;

    /** 该版本的文档正文 */
    private String content;

    /** 该版本的文档摘要 */
    private String summary;

    /** 关键词列表 */
    private List<String> keywords;

    /** 变更日志 */
    private String changeLog;

    /** 创建人标识 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createTime;
}
