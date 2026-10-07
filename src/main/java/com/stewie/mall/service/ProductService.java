package com.stewie.mall.service;

import com.stewie.mall.common.BizErrorCode;
import com.stewie.mall.entity.Product;
import com.stewie.mall.exception.BusinessException;
import com.stewie.mall.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品业务层。未来在这里加事务、缓存等逻辑。
 */
@Service
public class ProductService {

    private final ProductMapper productMapper;

    // 构造器注入(推荐做法,优于 @Autowired 字段注入)
    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> listAll() {
        return productMapper.findAll();
    }

    public Product getById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(BizErrorCode.NOT_FOUND);
        }
        return product;
    }

    public Product create(Product product) {
        productMapper.insert(product);
        // 插入后 product.id 已被 MyBatis 回填
        return product;
    }
}
