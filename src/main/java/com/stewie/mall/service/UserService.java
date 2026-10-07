package com.stewie.mall.service;

import com.stewie.mall.common.BizErrorCode;
import com.stewie.mall.dto.LoginResponse;
import com.stewie.mall.entity.User;
import com.stewie.mall.exception.BusinessException;
import com.stewie.mall.mapper.UserMapper;
import com.stewie.mall.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务层:注册、登录。
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** 注册:密码用 BCrypt 加盐哈希后存库,绝不存明文 */
    public void register(String username, String password) {
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(BizErrorCode.BAD_REQUEST.getCode(), "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(username);
        userMapper.insert(user);
    }

    /** 登录:校验密码,成功则签发 JWT */
    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(BizErrorCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        String token = JwtUtil.generate(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername());
    }

    public User getById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(BizErrorCode.NOT_FOUND);
        }
        return user;
    }
}
