package com.stewie.mall.dto;

/**
 * 登录响应体:返回 token 和用户基本信息。
 */
public record LoginResponse(String token, Long userId, String username) {
}
