package com.javaee.documentservice.dto;

import lombok.Data;

/**
 * 文档版本对比请求参数，指定待比较的原始版本与修订版本。
 */
@Data
public class DocumentCompareDTO {
    /** 原始版本号 */
    private Integer originalVersion;
    /** 修订版本号 */
    private Integer revisedVersion;
}
