package com.stewie.mall.mapper;

import com.stewie.mall.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口。
 */
@Mapper
public interface UserMapper {

    User findByUsername(String username);

    User findById(Long id);

    int insert(User user);
}
