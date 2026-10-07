package com.stewie.mall.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体,对应 users 表。
 * password 用 @JsonIgnore 防止序列化到前端(绝不能把密码哈希返回出去)。
 */
@Data
public class User {

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private LocalDateTime createdAt;
}
