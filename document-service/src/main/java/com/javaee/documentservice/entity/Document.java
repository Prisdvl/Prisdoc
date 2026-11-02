package com.javaee.documentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档实体，对应 document 表的一条元数据记录。
 *
 * <p>正文以 MinIO 对象为准，此处的 content 字段不参与日常读写。</p>
 */
@Data
@TableName("document")
public class Document {

    /** 文档 ID，UUID 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 文档标题 */
    private String title;

    /** 文档正文，当前服务以 MinIO 中的对象为准 */
    private String content;

    /** 文档摘要，创建或更新时截取正文前 200 字 */
    private String summary;

    /** 关键词，JSON 数组字符串 */
    private String keywords;

    /** 关联的源文件 ID */
    private String fileId;

    /** 文档创建者用户 ID */
    private Long userId;

    /** 正文所在的 MinIO 桶 */
    private String bucketName;

    /** 正文在 MinIO 中的对象名 */
    private String objectName;

    /** 状态：active-活跃，deleted-已删除 */
    private String status;

    /** 版本号，每次更新或恢复版本时递增 */
    private Integer version;

    /** 文档分类 */
    private String category;

    /** 标签，JSON 数组字符串 */
    private String tags;

    /** 创建人标识 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
