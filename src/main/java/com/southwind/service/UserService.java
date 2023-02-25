package com.southwind.service;

import com.southwind.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.southwind.form.UserLoginForm;
import com.southwind.form.UserRegisterForm;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author admin
 * @since 2021-11-22
 */
public interface UserService extends IService<User> {
    public User register(UserRegisterForm userRegisterForm);
    public User login(UserLoginForm userLoginForm);
}
