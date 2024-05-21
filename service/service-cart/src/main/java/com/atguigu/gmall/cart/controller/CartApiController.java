package com.atguigu.gmall.cart.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.atguigu.gmall.model.cart.CartInfo;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */
    @RequestMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request) {
        // 如何获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.addToCart(skuId, userId, skuNum);
        return Result.ok();
    }

    /**
     * 查询购物车
     * @param request
     * @return
     */
    @GetMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 获取临时用户Id
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.getCartList(userId, userTempId);
        return Result.ok(cartInfoList);
    }

    //  选中状态
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request) {

        String userId = AuthContextHolder.getUserId(request);
        //  判断
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法
        cartService.checkCart(userId, isChecked, skuId);
        return Result.ok();
    }

    /**
     * 删除
     *
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // 如何获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }

    @GetMapping("/allCheckCart/{isChecked}")
    public Result allCheckCart(@PathVariable Integer isChecked, HttpServletRequest request) {
        //  获取登录用户Id ---> 存储到请求头中!
        String userId = AuthContextHolder.getUserId(request);
        //  需要临时用户Id ，页面js 中，判断如果未登录，就会生成临时用户Id存储到cookie！
        if (org.springframework.util.StringUtils.isEmpty(userId)) {
            //  从请求头中获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }

        //  调用服务层方法.
        this.cartService.allCheckCart(isChecked, userId);

        //  默认返回.
        return Result.ok();
    }


    /**
     * 清空购物车.
     * @return
     */
    @GetMapping("clearCart")
    public Result clearCart(HttpServletRequest request) {
        //  获取登录用户Id ---> 存储到请求头中!
        String userId = AuthContextHolder.getUserId(request);
        //  需要临时用户Id ，页面js 中，判断如果未登录，就会生成临时用户Id存储到cookie！
        if (org.springframework.util.StringUtils.isEmpty(userId)) {
            //  从请求头中获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法.
        this.cartService.clearCart(userId);
        return Result.ok();
    }

    //  根据用户userId 获取送货清单
    @GetMapping("getCartCheckedList/{userId}")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId) {
        //  调用服务层方法.
        return this.cartService.getCartCheckedList(userId);
    }
}
