package com.stewie.mall.controller;

import com.stewie.mall.common.Result;
import com.stewie.mall.dto.CreateOrderRequest;
import com.stewie.mall.dto.OrderDetailResponse;
import com.stewie.mall.entity.Order;
import com.stewie.mall.entity.OrderItem;
import com.stewie.mall.service.OrderService;
import com.stewie.mall.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口。下单需要登录(被拦截器保护),用户 id 从 ThreadLocal 取,不靠前端传。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** POST /api/orders 下单(事务:扣库存 + 建单) */
    @PostMapping
    public Result<Order> create(@RequestBody @Valid CreateOrderRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(orderService.createOrder(userId, request.items()));
    }

    /** GET /api/orders 当前用户的订单列表 */
    @GetMapping
    public Result<List<Order>> list() {
        return Result.success(orderService.listByUser(UserContext.getUserId()));
    }

    /** GET /api/orders/{id} 订单详情(主单 + 明细) */
    @GetMapping("/{id}")
    public Result<OrderDetailResponse> detail(@PathVariable Long id) {
        Order order = orderService.getById(id);
        List<OrderItem> items = orderService.listItems(id);
        return Result.success(new OrderDetailResponse(order, items));
    }
}
