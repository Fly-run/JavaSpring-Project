package com.stewie.mall.controller;

import com.stewie.mall.common.Result;
import com.stewie.mall.entity.Product;
import com.stewie.mall.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口层。RESTful 风格,统一返回 Result。
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** GET /api/products 查询全部商品 */
    @GetMapping
    public Result<List<Product>> list() {
        return Result.success(productService.listAll());
    }

    /** GET /api/products/{id} 按 id 查询单个商品 */
    @GetMapping("/{id}")
    public Result<Product> get(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /** POST /api/products 新增商品,@Valid 触发参数校验 */
    @PostMapping
    public Result<Product> create(@RequestBody @Valid Product product) {
        return Result.success(productService.create(product));
    }
}
