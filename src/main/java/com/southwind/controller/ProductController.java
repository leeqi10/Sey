package com.southwind.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.southwind.entity.Cart;
import com.southwind.entity.Product;
import com.southwind.entity.User;
import com.southwind.exception.MMallException;
import com.southwind.result.ResponseEnum;
import com.southwind.service.CartService;
import com.southwind.service.ProductCategoryService;
import com.southwind.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author admin
 * @since 2021-11-22
 */
@Controller
@RequestMapping("/product")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private CartService cartService;

    /**
     * 商品列表
     * @param type
     * @param productCategoryId
     * @param session
     * @return
     */
    @GetMapping("/list/{type}/{id}")
    public ModelAndView list(
            @PathVariable("type") Integer type,
            @PathVariable("id") Integer productCategoryId,
            HttpSession session
    ){
        if(type == null || productCategoryId == null){
            log.info("【商品列表】参数为空");
            throw new MMallException(ResponseEnum.PARAMETER_NULL);
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("productList");
        modelAndView.addObject("productList", this.productService.findAllByTypeAndProductCategoryId(type, productCategoryId));
        //判断是否为登录用户
        User user = (User) session.getAttribute("user");
        if(user == null){
            //未登录
            modelAndView.addObject("cartList", new ArrayList<>());
        }else{
            //登录用户
            //查询该用户的购物车记录
            modelAndView.addObject("cartList", this.cartService.findVOListByUserId(user.getId()));
        }
        //商品分类
        modelAndView.addObject("list", this.productCategoryService.buildProductCategoryMenu());
        return modelAndView;
    }

    /**
     * 商品搜索
     * @param keyWord
     * @param session
     * @return
     */
    @PostMapping("/search")
    public ModelAndView search(String keyWord,HttpSession session){
        if(keyWord == null){
            log.info("【商品搜索】参数为空");
            throw new MMallException(ResponseEnum.PARAMETER_NULL);
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("productList");
        //模糊查询的数据
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyWord);
        modelAndView.addObject("productList",this.productService.list(queryWrapper));
        //判断是否为登录用户
        User user = (User) session.getAttribute("user");
        if(user == null){
            //未登录
            modelAndView.addObject("cartList", new ArrayList<>());
        }else{
            //登录用户
            //查询该用户的购物车记录
            modelAndView.addObject("cartList", this.cartService.findVOListByUserId(user.getId()));
        }
        //商品分类
        modelAndView.addObject("list", this.productCategoryService.buildProductCategoryMenu());
        return modelAndView;
    }

    /**
     * 商品详情
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/detail/{id}")
    public ModelAndView detail(@PathVariable("id") Integer id,HttpSession session){
        if(id == null){
            log.info("【商品详情】参数为空");
            throw new MMallException(ResponseEnum.PARAMETER_NULL);
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("productDetail");
        //判断是否为登录用户
        User user = (User) session.getAttribute("user");
        if(user == null){
            //未登录
            modelAndView.addObject("cartList", new ArrayList<>());
        }else{
            //登录用户
            //查询该用户的购物车记录
            modelAndView.addObject("cartList", this.cartService.findVOListByUserId(user.getId()));
        }
        //商品分类
        modelAndView.addObject("list", this.productCategoryService.buildProductCategoryMenu());
        //商品详情
        modelAndView.addObject("product", this.productService.getById(id));
        return modelAndView;
    }

}

