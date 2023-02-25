package com.southwind.mapper;

import com.southwind.entity.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2021-11-22
 */
public interface CartMapper extends BaseMapper<Cart> {
    public int update(Integer id,Integer quantity,Float cost);
    public Float getCostByUserId(Integer id);
}
