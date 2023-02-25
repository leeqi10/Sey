package com.southwind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.southwind.entity.*;
import com.southwind.exception.MMallException;
import com.southwind.mapper.*;
import com.southwind.result.ResponseEnum;
import com.southwind.service.CartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southwind.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author admin
 * @since 2021-11-22
 */
@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    @Transactional
    public Boolean add(Cart cart) {
        //添加购物车
        int insert = this.cartMapper.insert(cart);
        if(insert != 1){
            throw new MMallException(ResponseEnum.CART_ADD_ERROR);
        }
        //商品减库存
        Integer stock = this.productMapper.getStockById(cart.getProductId());
        if(stock == null){
            log.info("【添加购物车】商品不存在");
            throw new MMallException(ResponseEnum.PRODUCT_NOT_EXISTS);
        }
        if(stock == 0){
            log.info("【添加购物车】库存不足");
            throw new MMallException(ResponseEnum.PRODUCT_STOCK_ERROR);
        }
        Integer newStock = stock - cart.getQuantity();
        if(newStock < 0){
            log.info("【添加购物车】库存不足");
            throw new MMallException(ResponseEnum.PRODUCT_STOCK_ERROR);
        }
        this.productMapper.updateStockById(cart.getProductId(), newStock);
        return true;
    }

    @Override
    public List<CartVO> findVOListByUserId(Integer userId) {
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Cart> cartList = this.cartMapper.selectList(queryWrapper);
        List<CartVO> cartVOList = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = this.productMapper.selectById(cart.getProductId());
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(product, cartVO);
            BeanUtils.copyProperties(cart, cartVO);
            cartVOList.add(cartVO);
        }
        return cartVOList;
    }

    @Override
    @Transactional
    public Boolean update(Integer id, Integer quantity, Float cost) {
        //更新库存
        Cart cart = this.cartMapper.selectById(id);
        Integer oldQuantity = cart.getQuantity();
        if(quantity.equals(oldQuantity)){
            log.info("【更新购物车】参数错误");
            throw new MMallException(ResponseEnum.CART_UPDATE_PARAMETER_ERROR);
        }
        //查询商品库存
        Integer stock = this.productMapper.getStockById(cart.getProductId());
        Integer newStock = stock - (quantity - oldQuantity);
        if(newStock < 0){
            log.info("【更新购物车】商品库存错误");
            throw new MMallException(ResponseEnum.PRODUCT_STOCK_ERROR);
        }
        Integer integer = this.productMapper.updateStockById(cart.getProductId(), newStock);
        if(integer != 1){
            log.info("【更新购物车】更新商品库存失败");
            throw new MMallException(ResponseEnum.CART_UPDATE_STOCK_ERROR);
        }
        //更新数据
        int update = this.cartMapper.update(id, quantity, cost);
        if(update != 1){
            log.info("【更新购物车】更新失败");
            throw new MMallException(ResponseEnum.CART_UPDATE_ERROR);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean delete(Integer id) {
        //更新商品库存
        Cart cart = this.cartMapper.selectById(id);
        Integer stock = this.productMapper.getStockById(cart.getProductId());
        Integer newStock = stock + cart.getQuantity();
        Integer integer = this.productMapper.updateStockById(cart.getProductId(), newStock);
        if(integer != 1){
            log.info("【删除购物车】更新商品库存失败");
            throw new MMallException(ResponseEnum.CART_UPDATE_STOCK_ERROR);
        }
        //删除购物车数据
        int i = this.cartMapper.deleteById(id);
        if(i != 1){
            log.info("【删除购物车】删除失败");
            throw new MMallException(ResponseEnum.CART_REMOVE_ERROR);
        }
        return true;
    }

    @Override
    @Transactional
    public Orders commit(String userAddress, String address, String remark, User user) {
        //处理地址
        if(!userAddress.equals("newAddress")){
            address = userAddress;
        }else{
            int i = this.userAddressMapper.setDefault();
            if(i == 0){
                log.info("【确认订单】修饰默认地址失败");
                throw new MMallException(ResponseEnum.USER_ADDRESS_SET_DEFAULT_ERROR);
            }
            //将新地址存入数据库
            UserAddress userAddress1 = new UserAddress();
            userAddress1.setIsdefault(1);
            userAddress1.setUserId(user.getId());
            userAddress1.setRemark(remark);
            userAddress1.setAddress(address);
            int insert = this.userAddressMapper.insert(userAddress1);
            if(insert == 0){
                log.info("【确认订单】添加新地址失败");
                throw new MMallException(ResponseEnum.USER_ADDRESS_ADD_ERROR);
            }
        }
        //创建订单主表
        Orders orders = new Orders();
        orders.setUserId(user.getId());
        orders.setLoginName(user.getLoginName());
        orders.setUserAddress(address);
        orders.setCost(this.cartMapper.getCostByUserId(user.getId()));
        String seriaNumber = null;
        try {
            StringBuffer result = new StringBuffer();
            for(int i=0;i<32;i++) {
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            seriaNumber =  result.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        orders.setSerialnumber(seriaNumber);
        int insert = this.ordersMapper.insert(orders);
        if(insert != 1){
            log.info("【确认订单】创建订单主表失败");
            throw new MMallException(ResponseEnum.ORDERS_CREATE_ERROR);
        }
        //创建订单从表
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        List<Cart> carts = this.cartMapper.selectList(queryWrapper);
        for (Cart cart : carts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            int insert1 = this.orderDetailMapper.insert(orderDetail);
            if(insert1 == 0){
                log.info("【确认订单】创建订单详情失败");
                throw new MMallException(ResponseEnum.ORDER_DETAIL_CREATE_ERROR);
            }
        }
        //清空当前用户购物车
        QueryWrapper<Cart> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("user_id", user.getId());
        int delete = this.cartMapper.delete(queryWrapper1);
        if(delete == 0){
            log.info("【确认订单】清空用户购物车失败");
            throw new MMallException(ResponseEnum.CART_REMOVE_ERROR);
        }
        return orders;
    }
}
