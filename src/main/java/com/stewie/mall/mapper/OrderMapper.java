package com.stewie.mall.mapper;

import com.stewie.mall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单 Mapper 接口。
 */
@Mapper
public interface OrderMapper {

    int insert(Order order);

    Order findById(Long id);

    List<Order> findByUserId(Long userId);
}
