package com.atguigu.gmall.item.service;

import java.util.Map;

public interface ItemService {

    /**
     * 获取sku详情信息
     * @param skuId
     * @return
     */
    Map<String, Object> getSkuItem(Long skuId);
}
