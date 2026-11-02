package com.javaee.aiservice.skills;

import com.javaee.aiservice.agent.ChatService;
import com.javaee.aiservice.security.BucketPermissionService;
import com.javaee.aiservice.security.RequestUserContext;
import com.javaee.aiservice.service.MinIOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能注册表，按名称维护当前可用的技能。
 *
 * <p>构造时注册文件上传、文件下载与 HTML PPT 三个技能，技能名称即查找键。</p>
 */
@Component
public class SkillRegistry {

    private final Map<String, Skill> skills = new HashMap<>();

    @Autowired
    public SkillRegistry(MinIOService minIOService, ChatService chatService,
                         BucketPermissionService bucketPermissionService,
                         RequestUserContext requestUserContext) {
        // 注册技能
        registerSkill(new FileUploadSkill(minIOService, bucketPermissionService, requestUserContext));
        registerSkill(new FileDownloadSkill(minIOService, bucketPermissionService, requestUserContext));
        registerSkill(new HtmlPptSkill(chatService));
    }

    public void registerSkill(Skill skill) {
        skills.put(skill.getName(), skill);
    }

    public Skill getSkill(String name) {
        return skills.get(name);
    }

    public Map<String, Skill> getAllSkills() {
        return skills;
    }
}
