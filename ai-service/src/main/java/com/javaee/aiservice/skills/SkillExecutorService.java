package com.javaee.aiservice.skills;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 技能执行入口，按名称查找并调用具体技能。
 *
 * <p>技能名不存在时抛出 IllegalArgumentException，由调用方转换为业务错误。</p>
 */
@Service
public class SkillExecutorService {

    private final SkillRegistry skillRegistry;

    @Autowired
    public SkillExecutorService(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    public Object executeSkill(String skillName, Object... parameters) {
        Skill skill = skillRegistry.getSkill(skillName);
        if (skill == null) {
            throw new IllegalArgumentException("技能不存在: " + skillName);
        }
        return skill.execute(parameters);
    }

    public String getSkillDescription(String skillName) {
        Skill skill = skillRegistry.getSkill(skillName);
        if (skill == null) {
            throw new IllegalArgumentException("技能不存在: " + skillName);
        }
        return skill.getDescription();
    }
}
