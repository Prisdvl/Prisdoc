package com.javaee.documentservice.dto;

import lombok.Data;

/**
 * 文档查询请求参数，支持关键词与分类两种筛选方式。
 *
 * <p>关键词优先匹配标题与关键词字段；pageNum、pageSize 为预留分页字段，
 * 当前检索未启用分页。</p>
 */
@Data
public class DocumentQueryDTO {

    /** 文档标题，当前检索未使用 */
    private String title;

    /** 分类，关键词为空时生效 */
    private String category;

    /** 关键词，匹配标题与关键词字段 */
    private String keyword;

    /** 页码，当前检索未启用分页 */
    private Integer pageNum;

    /** 每页条数，当前检索未启用分页 */
    private Integer pageSize;
}
