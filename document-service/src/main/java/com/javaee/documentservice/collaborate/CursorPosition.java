package com.javaee.documentservice.collaborate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协同光标位置模型，记录用户在文档中的选区范围与上报时间。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPosition {

    /** 文档 ID */
    private String documentId;

    /** 光标所属用户 ID */
    private String userId;

    /** 光标所属用户名，用于其他协作者展示 */
    private String userName;

    /** 选区起点，取文档正文的字符下标 */
    private int from;

    /** 选区终点，与起点相同表示仅定位光标 */
    private int to;

    /** 上报时间戳，毫秒 */
    private long timestamp;
}