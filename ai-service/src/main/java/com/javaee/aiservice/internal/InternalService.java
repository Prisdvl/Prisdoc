package com.javaee.aiservice.internal;

import com.javaee.aiservice.security.RequestUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 内部服务代理层，作为 AI Agent 与外部系统交互的安全层。
 *
 * <p>负责权限校验、危险操作确认、审计记录与告警发送；危险技能必须携带服务端确认标记。</p>
 */
@Component
public class InternalService {

    private static final Logger log = LoggerFactory.getLogger(InternalService.class);

    @Autowired
    private RequestUserContext requestUserContext;

    private static final Set<String> DESTRUCTIVE_SKILLS = Set.of("file-restore", "file-version-switch");

    /**
     * 检查当前用户是否有权执行指定技能。
     *
     * <p>要求上下文用户与登录用户一致（管理员例外），危险技能还需已确认。</p>
     *
     * @param skillName 技能名称
     * @param context 执行上下文，含 userId 与确认标记
     * @return 是否有权限：true-允许执行，false-拒绝
     */
    public boolean hasPermission(String skillName, Map<String, Object> context) {
        log.debug("检查权限: skillName={}", skillName);

        String contextUserId = String.valueOf(context.getOrDefault("userId", ""));
        String authenticatedUserId = requestUserContext.getRequiredUserId();
        String role = requestUserContext.getCurrentRole();

        if (contextUserId.isBlank() || "default".equals(contextUserId) || "anonymous".equals(contextUserId)) {
            log.warn("权限检查失败，缺少有效用户上下文: skillName={}", skillName);
            return false;
        }

        if (!requestUserContext.isAdmin() && !authenticatedUserId.equals(contextUserId)) {
            log.warn("权限检查失败，用户上下文不匹配: authenticated={}, context={}, skillName={}",
                    authenticatedUserId, contextUserId, skillName);
            return false;
        }

        if (DESTRUCTIVE_SKILLS.contains(skillName) && !Boolean.TRUE.equals(context.get("confirmedAction"))) {
            log.warn("权限检查失败，危险操作缺少服务端确认: userId={}, skillName={}", contextUserId, skillName);
            return false;
        }

        log.info("用户权限检查通过: userId={}, role={}, skillName={}", contextUserId, role, skillName);
        return true;
    }

    /**
     * 记录技能执行的审计日志。
     *
     * @param skillName 技能名称
     * @param params 调用参数
     * @param result 执行结果
     */
    public void logAudit(String skillName, Map<String, Object> params, Map<String, Object> result) {
        log.info("审计记录: skillName={}, params={}, result={}", skillName, params, result);
    }

    /**
     * 发送告警。
     *
     * @param alertData 告警数据
     */
    public void sendAlert(Map<String, Object> alertData) {
        log.warn("发送告警: {}", alertData);
    }

    /**
     * 获取技能的描述信息，供规划器选择工具时参考。
     *
     * @param skillName 技能名称
     * @return 技能描述，含名称、说明与参数定义
     */
    public Map<String, Object> getSkillDescription(String skillName) {
        Map<String, Object> description = new HashMap<>();
        
        switch (skillName) {
            case "file-delete":
                description.put("name", "文件删除");
                description.put("description", "根据前端documentId或MinIO对象名删除文件并移入回收站");
                description.put("parameters", Map.of(
                    "bucketName", "存储桶名称（可选）",
                    "objectName", "对象名称（与documentId二选一）",
                    "documentId", "前端业务文档ID（与objectName二选一）"
                ));
                break;
            case "file-download":
                description.put("name", "文件下载");
                description.put("description", "下载指定文件");
                description.put("parameters", Map.of(
                    "bucketName", "存储桶名称（可选）",
                    "objectName", "对象名称（必填）"
                ));
                break;
            default:
                description.put("name", skillName);
                description.put("description", "未知技能");
        }
        
        return description;
    }
}
