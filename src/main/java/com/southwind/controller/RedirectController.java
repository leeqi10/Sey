package com.southwind.controller;

import com.southwind.entity.User;
import com.southwind.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class RedirectController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{url}")
    public ModelAndView redirect(@PathVariable("url") String url, HttpSession session){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(url);
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
        return modelAndView;
    }

    @GetMapping("/")
    public String main(){
        return "redirect:/productCategory/main";
    }

    //解决一些报错信息，加载不到文件的错误，就返回空，解决不必要的报错
    @GetMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
    }

}