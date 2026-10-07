package com.stewie.mall.mapper;

import com.stewie.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品 Mapper 接口。
 * SQL 写在 resources/mapper/ProductMapper.xml 里(也可用注解,但 XML 更贴近企业实战)。
 */
@Mapper
public interface ProductMapper {

    List<Product> findAll();

    Product findById(Long id);

    int insert(Product product);
}
