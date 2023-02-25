package com.southwind.mapper;

import com.southwind.entity.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2021-11-22
 */
public interface UserAddressMapper extends BaseMapper<UserAddress> {
    public int setDefault();
}
