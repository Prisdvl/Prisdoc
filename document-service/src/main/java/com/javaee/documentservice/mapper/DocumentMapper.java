package com.javaee.documentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.documentservice.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档数据访问接口，提供文档的检索与筛选查询。
 *
 * <p>查询默认只返回 status 为 active 的文档，并按创建时间倒序排列。</p>
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 查询用户本人创建的活跃文档。
     *
     * @param userId 用户 ID
     * @return 文档列表
     */
    List<Document> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户可访问的活跃文档，含本人创建与被授权协作的文档。
     *
     * @param userId 用户 ID
     * @return 去重后的文档列表
     */
    List<Document> selectAccessibleByUserId(@Param("userId") Long userId);

    /**
     * 在用户可访问的范围内检索文档。
     *
     * <p>关键词匹配标题与关键词字段；关键词为空时才按分类筛选。</p>
     *
     * @param userId 用户 ID
     * @param keyword 关键词，可为空
     * @param category 分类，可为空
     * @return 命中的文档列表
     */
    List<Document> searchAccessible(@Param("userId") Long userId,
                                    @Param("keyword") String keyword,
                                    @Param("category") String category);

    /**
     * 按分类查询活跃文档。
     *
     * @param category 分类
     * @return 该分类下的文档列表
     */
    List<Document> selectByCategory(@Param("category") String category);

    /**
     * 按关键词匹配标题或关键词字段查询活跃文档。
     *
     * @param keyword 关键词
     * @return 命中的文档列表
     */
    List<Document> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 按状态查询文档。
     *
     * @param status 文档状态，如 active、deleted
     * @return 该状态下的文档列表
     */
    List<Document> selectByStatus(@Param("status") String status);
}
