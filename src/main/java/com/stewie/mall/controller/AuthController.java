package com.stewie.mall.controller;

import com.stewie.mall.common.Result;
import com.stewie.mall.dto.LoginRequest;
import com.stewie.mall.dto.LoginResponse;
import com.stewie.mall.dto.RegisterRequest;
import com.stewie.mall.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口:注册、登录。这两个接口在白名单里,不需要登录就能访问。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request.username(), request.password());
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(userService.login(request.username(), request.password()));
    }
}
