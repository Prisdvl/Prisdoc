package com.javaee.fileservice.security;

import com.javaee.common.utils.UserBucketUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 存储桶访问权限校验服务，按当前用户的权限组判断其能否访问指定 MinIO 存储桶。
 *
 * <p>管理员与用户自有桶直接放行；其余情况按 minio.bucket-permissions 配置的
 * 「桶名=权限组」映射比对，桶未配置或未解析到权限组时取
 * minio.bucket-permission-default-allow 作为结果。</p>
 */
@Service
public class BucketPermissionService {

    @Value("${minio.bucket-permissions:}")
    private String bucketPermissions;

    @Value("${minio.bucket-permission-default-allow:false}")
    private boolean defaultAllow;

    /**
     * 校验当前用户能否访问指定存储桶，无权访问时抛出异常。
     *
     * @param bucketName 待校验的存储桶名称
     * @throws SecurityException 桶名为空或当前用户无权访问该桶
     */
    public void assertCanAccess(String bucketName) {
        if (isAdmin()) {
            return;
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new SecurityException("存储桶名称不能为空");
        }
        if (isCurrentUserBucket(bucketName)) {
            return;
        }
        if (isAllowed(bucketName, currentPermissionGroups())) {
            return;
        }
        throw new SecurityException("无权访问存储桶: " + bucketName);
    }

    boolean isAllowed(String bucketName, Set<String> userGroups) {
        if (bucketPermissions == null || bucketPermissions.isBlank()) {
            return defaultAllow;
        }
        Set<String> allowedGroups = parseGroups(permissionsForBucket(bucketName));
        if (allowedGroups.isEmpty()) {
            return defaultAllow;
        }
        Set<String> normalizedUserGroups = normalizeGroups(userGroups);
        return normalizedUserGroups.contains("*") || allowedGroups.contains("*")
                || allowedGroups.stream().anyMatch(normalizedUserGroups::contains);
    }

    private boolean isAdmin() {
        return currentPermissionGroups().contains("admin");
    }

    private boolean isCurrentUserBucket(String bucketName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            return false;
        }
        return UserBucketUtils.isUserBucket(bucketName, authentication.getPrincipal());
    }

    private Set<String> currentPermissionGroups() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && !authority.isBlank())
                .map(authority -> authority.replaceFirst("^ROLE_", "").replaceFirst("^GROUP_", ""))
                .map(this::normalizeGroup)
                .collect(Collectors.toSet());
    }

    private Set<String> parseGroups(String groups) {
        if (groups == null || groups.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(groups.split("[,;]"))
                .map(this::normalizeGroup)
                .filter(group -> !group.isBlank())
                .collect(Collectors.toSet());
    }

    private String permissionsForBucket(String bucketName) {
        String normalizedBucket = normalizeGroup(bucketName);
        for (String entry : bucketPermissions.split("[;\\n]")) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && normalizedBucket.equals(normalizeGroup(parts[0]))) {
                return parts[1];
            }
        }
        return "";
    }

    private Set<String> normalizeGroups(Set<String> groups) {
        if (groups == null || groups.isEmpty()) {
            return Set.of();
        }
        Set<String> normalized = new HashSet<>();
        for (String group : groups) {
            String value = normalizeGroup(group);
            if (!value.isBlank()) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private String normalizeGroup(String group) {
        return group == null ? "" : group.trim().toLowerCase(Locale.ROOT);
    }
}
