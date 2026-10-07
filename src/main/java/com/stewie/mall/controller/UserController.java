package com.stewie.mall.controller;

import com.stewie.mall.common.Result;
import com.stewie.mall.entity.User;
import com.stewie.mall.service.UserService;
import com.stewie.mall.util.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。这里是被 JWT 拦截器保护的路径,不带 token 会返回 401。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/user/me 返回当前登录用户信息(用户 id 从 ThreadLocal 拿) */
    @GetMapping("/me")
    public Result<User> me() {
        Long userId = UserContext.getUserId();
        return Result.success(userService.getById(userId));
    }
}
