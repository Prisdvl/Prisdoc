package com.javaee.documentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档协作访问实体，记录某个用户对某篇文档的角色授权。
 *
 * <p>「文档 ID + 用户 ID」唯一，重复授权时更新角色而不是新增记录。</p>
 */
@Data
@TableName("document_access")
public class DocumentAccess {

    /** 授权记录 ID，UUID 主键 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 文档 ID */
    private String documentId;

    /** 文档所在的 MinIO 桶 */
    private String bucketName;

    /** 被授权的用户 ID */
    private Long userId;

    /** 协作角色：owner、editor、viewer */
    private String role;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
