package com.stewie.mall.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 下单请求体。items 里每项指定商品 id 和数量。
 */
public record CreateOrderRequest(
        @NotEmpty(message = "订单明细不能为空")
        @Valid
        List<Item> items
) {

    public record Item(
            @NotNull(message = "商品ID不能为空")
            Long productId,

            @NotNull(message = "数量不能为空")
            @Min(value = 1, message = "数量至少为 1")
            Integer quantity
    ) {
    }
}
