package com.javaee.documentservice.dto;

import lombok.Data;

import java.util.List;

/**
 * 文档更新请求参数，只提交需要变更的字段。
 *
 * <p>content 非空时覆盖正文并刷新摘要，更新前的正文会先存为历史版本。</p>
 */
@Data
public class DocumentUpdateDTO {

    /** 文档 ID，实际以路径参数为准 */
    private String id;

    /** 新标题，为空时保持原值 */
    private String title;

    /** 新正文，可为空串；为 {@code null} 时不更新正文 */
    private String content;

    /** 新分类，为 {@code null} 时保持原值 */
    private String category;

    /** 新标签列表，为 {@code null} 时保持原值 */
    private List<String> tags;

    /** 本次变更说明，写入历史版本的变更日志 */
    private String changeLog;
}
