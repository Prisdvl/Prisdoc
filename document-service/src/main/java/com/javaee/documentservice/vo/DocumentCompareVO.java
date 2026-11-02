package com.javaee.documentservice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文档版本对比结果，包含逐行变更明细与增删改行数统计。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCompareVO {
    /** 对比结论摘要 */
    private String summary;
    /** 逐行变更明细 */
    private List<ChangeDetail> changes;
    /** 新增行数 */
    private Integer addedLines;
    /** 删除行数 */
    private Integer removedLines;
    /** 修改行数 */
    private Integer modifiedLines;
    /** 原始版本号 */
    private Integer originalVersion;
    /** 修订版本号 */
    private Integer revisedVersion;

    /**
     * 单行变更明细，记录变更类型与原始、修订两侧的文本。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeDetail {
        /** 变更类型：added-新增，removed-删除，modified-修改 */
        private String type;
        /** 变更所在行号 */
        private Integer lineNumber;
        /** 原始版本中的文本 */
        private String originalText;
        /** 修订版本中的文本 */
        private String revisedText;
        /** 变更说明 */
        private String description;
    }
}
