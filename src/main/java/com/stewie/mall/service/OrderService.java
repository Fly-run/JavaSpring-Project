package com.stewie.mall.service;

import com.stewie.mall.common.BizErrorCode;
import com.stewie.mall.dto.CreateOrderRequest;
import com.stewie.mall.entity.Order;
import com.stewie.mall.entity.OrderItem;
import com.stewie.mall.entity.Product;
import com.stewie.mall.exception.BusinessException;
import com.stewie.mall.mapper.OrderItemMapper;
import com.stewie.mall.mapper.OrderMapper;
import com.stewie.mall.mapper.ProductMapper;
import com.stewie.mall.util.CacheClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单业务层。下单是核心:扣库存 + 建单 + 写明细必须在一个事务里,任一环节失败都整体回滚。
 */
@Service
public class OrderService {

    /** 商品详情缓存 key 前缀,需与 ProductService 保持一致 */
    private static final String PRODUCT_CACHE_PREFIX = "cache:product:";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CacheClient cacheClient;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                        ProductMapper productMapper, CacheClient cacheClient) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.cacheClient = cacheClient;
    }

    /**
     * 下单。
     * @Transactional:扣库存、建主单、写明细绑定成一个事务,异常则全部回滚。
     * SELECT ... FOR UPDATE 对商品行加悲观锁,防止并发下单超卖。
     */
    @Transactional
    public Order createOrder(Long userId, List<CreateOrderRequest.Item> items) {
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 1. 逐项锁行、校验库存、扣库存、累加总价
        for (CreateOrderRequest.Item item : items) {
            Product product = productMapper.findForUpdate(item.productId());
            if (product == null) {
                throw new BusinessException(BizErrorCode.NOT_FOUND.getCode(), "商品不存在,id=" + item.productId());
            }
            if (product.getStock() < item.quantity()) {
                throw new BusinessException(BizErrorCode.BAD_REQUEST.getCode(), "库存不足:" + product.getName());
            }
            // 双保险:UPDATE 带 stock >= quantity 条件,并发下也不会扣成负数
            if (productMapper.deductStock(item.productId(), item.quantity()) == 0) {
                throw new BusinessException(BizErrorCode.BAD_REQUEST.getCode(), "库存不足:" + product.getName());
            }

            // 缓存一致性:扣库存后删除商品缓存。必须等事务提交后再删,
            // 否则并发请求会回源读到"未提交的旧库存"又把旧值写回缓存。
            Long productId = item.productId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheClient.delete(PRODUCT_CACHE_PREFIX + productId);
                }
            });

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setUnitPrice(product.getPrice());
            oi.setQuantity(item.quantity());
            orderItems.add(oi);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }

        // 2. 建主单(insert 后 order.id 已被回填)
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(0);
        orderMapper.insert(order);

        // 3. 写明细
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        return order;
    }

    public List<Order> listByUser(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    public Order getById(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(BizErrorCode.NOT_FOUND);
        }
        return order;
    }

    public List<OrderItem> listItems(Long orderId) {
        return orderItemMapper.findByOrderId(orderId);
    }
}
