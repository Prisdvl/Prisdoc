package com.javaee.documentservice.dto;

import lombok.Data;

import java.util.List;

/**
 * 文档创建请求参数，采用文件 ID 模式引用已上传的源文件。
 *
 * <p>创建时按 fileId 从 file-service 下载并解析正文，因此请求体不直接携带正文。</p>
 */
@Data
public class DocumentCreateDTO {

    /** 文档标题 */
    private String title;

    /** 源文件 ID，可为空；为空时只创建元数据 */
    private String fileId;

    /** 文档分类 */
    private String category;

    /** 标签列表 */
    private List<String> tags;
}
