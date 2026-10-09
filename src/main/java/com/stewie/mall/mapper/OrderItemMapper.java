package com.stewie.mall.mapper;

import com.stewie.mall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单明细 Mapper 接口。
 */
@Mapper
public interface OrderItemMapper {

    int insert(OrderItem orderItem);

    List<OrderItem> findByOrderId(Long orderId);
}
