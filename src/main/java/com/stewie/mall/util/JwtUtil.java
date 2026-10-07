package com.stewie.mall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类:签发和解析 token。
 * HS256 签名,密钥需 >= 32 字节。
 */
public final class JwtUtil {

    private static final String SECRET = "stewie-mall-secret-key-0123456789-0123456789";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /** token 有效期:24 小时 */
    private static final long EXPIRE_MILLIS = 1000L * 60 * 60 * 24;

    private JwtUtil() {
    }

    /** 签发 token,把用户 id 作为 subject 存进去 */
    public static String generate(Long userId, String username) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MILLIS))
                .signWith(KEY)
                .compact();
    }

    /** 解析 token。过期或签名不合法会抛异常,由调用方捕获。 */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
