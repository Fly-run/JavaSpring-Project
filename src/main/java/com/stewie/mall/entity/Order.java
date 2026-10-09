package com.stewie.mall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体,对应 orders 表。
 * status:0=待支付(后续可扩展 1=已支付 2=已取消)。
 */
@Data
public class Order {

    private Long id;

    private Long userId;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime createdAt;
}
