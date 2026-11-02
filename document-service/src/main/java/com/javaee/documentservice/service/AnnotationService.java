package com.javaee.documentservice.service;

import com.javaee.documentservice.dto.AnnotationCreateDTO;
import com.javaee.documentservice.vo.AnnotationVO;

import java.util.List;

/**
 * 批注服务接口，定义文档批注的增删改查能力。
 *
 * <p>批注以文档内字符区间或行号定位，删除后仅置为失效状态而不物理删除。</p>
 */
public interface AnnotationService {

    /**
     * 创建批注。
     *
     * @param dto 批注创建参数
     * @param userId 创建用户 ID
     * @return 新建的批注信息
     */
    AnnotationVO createAnnotation(AnnotationCreateDTO dto, Long userId);

    /**
     * 查询文档下的全部批注。
     *
     * @param documentId 文档 ID
     * @return 批注列表
     */
    List<AnnotationVO> getAnnotationsByDocumentId(String documentId);

    /**
     * 按批注 ID 查询批注详情。
     *
     * @param id 批注 ID
     * @return 批注详情；批注不存在时抛出业务异常
     */
    AnnotationVO getAnnotationById(String id);

    /**
     * 更新批注内容。
     *
     * @param id 批注 ID
     * @param dto 更新的批注参数
     * @param userId 操作用户 ID，需为批注创建者
     */
    void updateAnnotation(String id, AnnotationCreateDTO dto, Long userId);

    /**
     * 删除批注。
     *
     * @param id 批注 ID
     * @param userId 操作用户 ID，需为批注创建者
     */
    void deleteAnnotation(String id, Long userId);

    /**
     * 查询文档指定行的全部批注。
     *
     * @param documentId 文档 ID
     * @param lineNumber 行号
     * @return 该行的批注列表
     */
    List<AnnotationVO> getAnnotationsByLineNumber(String documentId, Integer lineNumber);
}
