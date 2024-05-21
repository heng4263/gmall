package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartService {


    /**
     *  添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(Long skuId, String userId, Integer skuNum);

    /**
     * 通过用户Id 查询购物车列表
     * @param userId
     * @param userTempId
     * @return
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * 更新选中状态
     * @param userId
     * @param isChecked
     * @param skuId
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除单个购物项
     * @param skuId
     * @param userId
     */
    void deleteCart(Long skuId, String userId);

    /**
     * 购物车全选功能.
     * @param isChecked
     * @param userId
     */
    void allCheckCart(Integer isChecked, String userId);

    /**
     * 清空购物车.
     * @param userId
     */
    void clearCart(String userId);

    /**
     * 根据用户Id 查询选中的购物项
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);


}
