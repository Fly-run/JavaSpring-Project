package com.stewie.mall.dto;

import com.stewie.mall.entity.Order;
import com.stewie.mall.entity.OrderItem;

import java.util.List;

/**
 * 订单详情返回体:主单 + 明细列表。
 */
public record OrderDetailResponse(Order order, List<OrderItem> items) {
}
