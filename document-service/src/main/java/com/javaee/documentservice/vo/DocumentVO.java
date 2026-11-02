package com.javaee.documentservice.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档展示对象，向前端返回文档元数据，按需携带正文。
 *
 * <p>列表与搜索接口不填充 content，详情接口才回填正文。</p>
 */
@Data
public class DocumentVO {

    /** 文档 ID */
    private String id;

    /** 文档标题 */
    private String title;

    /** 文档正文，仅详情接口返回 */
    private String content;

    /** 文档摘要 */
    private String summary;

    /** 关键词列表 */
    private List<String> keywords;

    /** 关联的源文件 ID */
    private String fileId;

    /** 文档创建者用户 ID */
    private Long userId;

    /** 正文所在的 MinIO 桶 */
    private String bucketName;

    /** 正文在 MinIO 中的对象名 */
    private String objectName;

    /** 文档分类 */
    private String category;

    /** 标签列表 */
    private List<String> tags;

    /** 版本号 */
    private Integer version;

    /** 状态：active-活跃，deleted-已删除 */
    private String status;

    /** 创建人标识 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
