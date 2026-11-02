package com.javaee.documentservice.security;

import com.javaee.common.exception.BusinessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 当前用户解析器，从 JWT 安全上下文或网关请求头中取出用户 ID。
 *
 * <p>优先读取安全上下文中的 Principal，未认证时回落到 {@code X-User-Id} 请求头；
 * 解析结果统一为 {@link Long}，无法解析时视为未认证。</p>
 */
@Component
public class RequestUserContext {

    /**
     * 获取当前登录用户 ID，未认证时直接拒绝请求。
     *
     * @return 当前用户 ID
     */
    public Long getRequiredUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new BusinessException("用户未认证，请先登录"));
    }

    /**
     * 尝试获取当前用户 ID，不抛异常。
     *
     * @return 用户 ID；未认证或无法解析时为空
     */
    public Optional<Long> getCurrentUserId() {
        Optional<Long> principalUserId = userIdFromPrincipal();
        if (principalUserId.isPresent()) {
            return principalUserId;
        }
        return userIdFromHeader();
    }

    private Optional<Long> userIdFromPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            return Optional.empty();
        }
        return parseUserId(authentication.getPrincipal().toString());
    }

    private Optional<Long> userIdFromHeader() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }
        return parseUserId(attributes.getRequest().getHeader("X-User-Id"));
    }

    private Optional<Long> parseUserId(String raw) {
        if (raw == null || raw.isBlank() || "anonymousUser".equalsIgnoreCase(raw)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(raw.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
