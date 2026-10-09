package com.stewie.mall.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stewie.mall.common.BizErrorCode;
import com.stewie.mall.common.PageResult;
import com.stewie.mall.entity.Product;
import com.stewie.mall.exception.BusinessException;
import com.stewie.mall.mapper.ProductMapper;
import com.stewie.mall.util.CacheClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品业务层。getById 接入了 Redis 缓存,覆盖缓存穿透/击穿/雪崩三大经典问题。
 */
@Service
public class ProductService {

    /** 商品详情缓存 key 前缀 */
    private static final String CACHE_KEY_PREFIX = "cache:product:";
    /** 商品详情缓存 TTL:30 分钟(CacheClient 内部会随机浮动防雪崩) */
    private static final long CACHE_TTL_SECONDS = 30 * 60;
    /** 空值缓存 TTL:1 分钟,防缓存穿透 */
    private static final long EMPTY_TTL_SECONDS = 60;
    /** 击穿互斥锁 TTL:10 秒 */
    private static final long LOCK_TTL_SECONDS = 10;
    /** 未抢到锁时的重试次数 */
    private static final int RETRY_TIMES = 3;

    private final ProductMapper productMapper;
    private final CacheClient cacheClient;

    // 构造器注入(推荐做法,优于 @Autowired 字段注入)
    public ProductService(ProductMapper productMapper, CacheClient cacheClient) {
        this.productMapper = productMapper;
        this.cacheClient = cacheClient;
    }

    /**
     * 分页查询商品列表。
     * PageHelper.startPage 只是往 ThreadLocal 里放分页参数,紧跟其后的第一条查询
     * 会被 PageInterceptor 自动改写:先发 COUNT 查总数,再发带 LIMIT 的数据查询。
     */
    public PageResult<Product> listPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.findAll();
        return PageResult.of(new PageInfo<>(list));
    }

    /**
     * 按 id 查单个商品,带 Redis 缓存:
     * 1. 先查缓存,命中空值缓存直接抛 404(防穿透);
     * 2. 缓存未命中则抢互斥锁,只有抢到锁的线程回源重建(防击穿);
     * 3. 没抢到锁的线程短暂等待后重试查缓存;
     * 4. TTL 随机浮动(防雪崩)在 CacheClient 里统一处理。
     */
    public Product getById(Long id) {
        String key = CACHE_KEY_PREFIX + id;

        // 1. 先查缓存
        String json = cacheClient.get(key);
        if (json != null) {
            return parseOrThrow(key, json);
        }

        // 2. 缓存未命中 → 互斥锁防击穿
        String lockKey = "lock:product:" + id;
        if (cacheClient.tryLock(lockKey, LOCK_TTL_SECONDS)) {
            try {
                // 双重检查:拿到锁的间隙可能别的线程已经重建好了
                json = cacheClient.get(key);
                if (json != null) {
                    return parseOrThrow(key, json);
                }
                return rebuildCache(key, id);
            } finally {
                cacheClient.unlock(lockKey);
            }
        }

        // 3. 没抢到锁 → 短暂等待后重试,仍 miss 则兜底回源
        return retryGetById(key, id, RETRY_TIMES);
    }

    /** 回源查 DB 并重建缓存;DB 里没有则缓存空值(防穿透) */
    private Product rebuildCache(String key, Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            cacheClient.setEmpty(key, EMPTY_TTL_SECONDS);
            throw new BusinessException(BizErrorCode.NOT_FOUND);
        }
        cacheClient.set(key, product, CACHE_TTL_SECONDS);
        return product;
    }

    /** 未抢到锁的重试逻辑,重试后仍 miss 则兜底回源 */
    private Product retryGetById(String key, Long id, int retries) {
        for (int i = 0; i < retries; i++) {
            sleep(50);
            String json = cacheClient.get(key);
            if (json != null) {
                return parseOrThrow(key, json);
            }
        }
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(BizErrorCode.NOT_FOUND);
        }
        cacheClient.set(key, product, CACHE_TTL_SECONDS);
        return product;
    }

    /** 解析缓存:空串=命中空值缓存;反序列化失败则删脏缓存并抛 500 */
    private Product parseOrThrow(String key, String json) {
        if (json.isEmpty()) {
            throw new BusinessException(BizErrorCode.NOT_FOUND);
        }
        Product product = cacheClient.fromJson(json, Product.class);
        if (product == null) {
            cacheClient.delete(key);
            throw new BusinessException(BizErrorCode.SERVER_ERROR);
        }
        return product;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Product create(Product product) {
        productMapper.insert(product);
        // 插入后 product.id 已被 MyBatis 回填;新 id 从未被缓存过,无需删缓存
        return product;
    }
}
