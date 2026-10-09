package com.stewie.mall.mapper;

import com.stewie.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /** 按 id 锁定商品行(FOR UPDATE),用于下单时的悲观锁 */
    Product findForUpdate(Long id);

    /** 扣库存,带 stock >= quantity 条件,影响行数为 0 表示库存不足 */
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
