package com.javaee.documentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档版本实体，保存文档在某一时刻的正文与元数据快照。
 *
 * <p>创建、更新与恢复版本时各写入一条，版本内容整体落库以便回滚。</p>
 */
@Data
@TableName("document_version")
public class DocumentVersion {

    /** 版本记录 ID，UUID 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 所属文档 ID */
    private String documentId;

    /** 版本号，与文档当前版本对应 */
    private Integer versionNumber;

    /** 该版本的文档标题 */
    private String title;

    /** 该版本的文档正文 */
    private String content;

    /** 该版本的文档摘要 */
    private String summary;

    /** 该版本的关键词 */
    private String keywords;

    /** 变更日志，说明本次版本产生的原因 */
    private String changeLog;

    /** 创建人标识 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createTime;
}
