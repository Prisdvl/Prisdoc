package com.javaee.fileservice.config;

import com.javaee.common.utils.UserBucketUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 文件存储配置类，从配置项读取存储类型、本地路径、大小上限与存储桶名称。
 *
 * <p>存储桶名称优先取当前登录用户的专属桶，取不到用户时回退到配置的默认桶。</p>
 */
@Configuration
public class FileStorageConfig {

    @Value("${file.storage.type}")
    private String storageType;

    @Value("${file.storage.local-path}")
    private String localPath;

    @Value("${file.storage.max-size}")
    private long maxSize;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String getStorageType() {
        return storageType;
    }

    public String getLocalPath() {
        return localPath;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public String getBucketName() {
        return currentUserId()
                .map(UserBucketUtils::bucketNameForUser)
                .orElse(bucketName);
    }

    public String getConfiguredBucketName() {
        return bucketName;
    }

    private Optional<String> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getPrincipal().toString())) {
            return Optional.of(authentication.getPrincipal().toString());
        }
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String headerUserId = attributes.getRequest().getHeader("X-User-Id");
            if (headerUserId != null && !headerUserId.isBlank()) {
                return Optional.of(headerUserId.trim());
            }
        }
        return Optional.empty();
    }

}
