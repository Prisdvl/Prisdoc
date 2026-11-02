package com.javaee.aiservice.skills;

/**
 * 技能统一接口，约束技能的自描述与执行入口。
 *
 * <p>技能由 SkillRegistry 按名称注册，名称需唯一且稳定。</p>
 */
public interface Skill {
    String getName();
    String getDescription();
    Object execute(Object... parameters);
}
