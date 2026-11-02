package com.javaee.common.utils;

import com.javaee.common.constant.CommonConstant;
import com.javaee.common.constant.ErrorCodeEnum;
import com.javaee.common.exception.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具，负责访问令牌与刷新令牌的签发、解析与校验。
 *
 * <p>令牌使用 HS256 密钥签名，访问令牌有效期 30 分钟，刷新令牌有效期 7 天；
 * 解析失败统一抛出令牌异常。</p>
 *
 * @author qxk
 */
public class JwtUtils {

    private static final String SECRET_KEY = "prisdoc-secret-key-for-jwt-token-generation-and-validation-2024";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    private static final long TOKEN_EXPIRATION = 30 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成包含用户 ID 与用户名的访问令牌。
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @return 签名后的访问令牌
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstant.TOKEN_CLAIM_USER_ID, userId);
        claims.put(CommonConstant.TOKEN_CLAIM_USERNAME, username);
        return generateToken(claims, TOKEN_EXPIRATION);
    }

    /**
     * 生成包含用户 ID、用户名与角色的访问令牌。
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @param role 用户角色
     * @return 签名后的访问令牌
     */
    public static String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstant.TOKEN_CLAIM_USER_ID, userId);
        claims.put(CommonConstant.TOKEN_CLAIM_USERNAME, username);
        claims.put(CommonConstant.TOKEN_CLAIM_ROLE, role);
        return generateToken(claims, TOKEN_EXPIRATION);
    }

    /**
     * 生成刷新令牌。
     *
     * @param userId 用户 ID
     * @return 有效期 7 天的刷新令牌
     */
    public static String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstant.TOKEN_CLAIM_USER_ID, userId);
        return generateToken(claims, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * 按声明与有效期签发令牌。
     *
     * @param claims 令牌声明
     * @param expiration 有效期，单位毫秒
     * @return 签名后的令牌
     */
    private static String generateToken(Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析并校验令牌，返回其中的声明。
     *
     * @param token 待解析的令牌
     * @return 令牌声明
     * @throws TokenException 令牌非法、签名错误或已过期
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new TokenException(ErrorCodeEnum.TOKEN_ERROR);
        }
    }

    /**
     * 判断令牌是否有效。
     *
     * @param token 待校验的令牌
     * @return true-签名有效且未过期
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中读取用户 ID。
     *
     * @param token 令牌
     * @return 用户 ID；声明缺失时返回 null
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get(CommonConstant.TOKEN_CLAIM_USER_ID, Long.class);
    }

    /**
     * 从令牌中读取用户名。
     *
     * @param token 令牌
     * @return 用户名；声明缺失时返回 null
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get(CommonConstant.TOKEN_CLAIM_USERNAME, String.class);
    }

    /**
     * 从令牌中读取角色。
     *
     * @param token 令牌
     * @return 角色；声明缺失时返回 null
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get(CommonConstant.TOKEN_CLAIM_ROLE, String.class);
    }

    /**
     * 判断令牌是否已过期。
     *
     * @param token 待判断的令牌
     * @return true-已过期或无法解析
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从 Authorization 请求头中提取令牌。
     *
     * @param authHeader 请求头值
     * @return 去掉 Bearer 前缀后的令牌
     * @throws TokenException 请求头缺失或前缀不匹配
     */
    public static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(CommonConstant.TOKEN_PREFIX)) {
            return authHeader.substring(CommonConstant.TOKEN_PREFIX.length());
        }
        throw new TokenException(ErrorCodeEnum.TOKEN_ERROR);
    }
}
