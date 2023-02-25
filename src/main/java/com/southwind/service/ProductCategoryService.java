package com.southwind.service;

import com.southwind.entity.Product;
import com.southwind.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.southwind.vo.ProductCategoryVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author admin
 * @since 2021-11-22
 */
public interface ProductCategoryService extends IService<ProductCategory> {
    public List<ProductCategoryVO> buildProductCategoryMenu();
    public List<ProductCategoryVO> findAllProductByCategoryLevelOne();
}
