package com.stewie.mall.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体,对应 order_items 表。
 * 存 product_name / unit_price 快照:即使商品后续改名或调价,历史订单也不受影响。
 */
@Data
public class OrderItem {

    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private BigDecimal unitPrice;

    private Integer quantity;
}
