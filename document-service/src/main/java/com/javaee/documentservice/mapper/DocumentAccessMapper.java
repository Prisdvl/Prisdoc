package com.javaee.documentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javaee.documentservice.entity.DocumentAccess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档协作访问关系数据访问接口，基于 MyBatis-Plus 完成授权记录的增删改查。
 *
 * <p>授权与校验逻辑由服务层控制，此处不额外定义 SQL。</p>
 */
@Mapper
public interface DocumentAccessMapper extends BaseMapper<DocumentAccess> {
}
