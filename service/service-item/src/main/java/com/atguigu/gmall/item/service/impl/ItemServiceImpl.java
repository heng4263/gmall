package com.atguigu.gmall.item.service.impl;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    //  Feign远程调用service-product-client
    @Autowired
    private ProductFeignClient productFeignClient;
    // 线程池
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    //  Feign远程调用service-list-client
    @Autowired
    private ListFeignClient listFeignClient;


    @Override
    public Map<String, Object> getSkuItem(Long skuId) {
        //声明一个map 集合
        Map<String, Object> result = new HashMap<>();


        //  获取到数据.skuInfo 需要一个返回值方法：
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            // 保存skuInfo
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        //  获取分类数据
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        }, threadPoolExecutor);

        // 销售属性-(销售属性值)回显并锁定
        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);

        //根据spuId 查询map 集合属性
        //(销售属性)-销售属性值回显并锁定
        CompletableFuture<Void> strJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //  将这个map 转换为页面需要的Json 对象
            String valueJson = JSON.toJSONString(skuValueIdsMap);
            result.put("valuesSkuJson", valueJson);
        }, threadPoolExecutor);

        //  获取价格
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            result.put("price", skuPrice);
        }, threadPoolExecutor);

        //  spu海报数据
        CompletableFuture<Void> spuPosterCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterList = productFeignClient.getSpuPosterBySpuId(skuInfo.getSpuId());
            result.put("spuPosterList", spuPosterList);
        }, threadPoolExecutor);

        //  获取平台属性---规格参数.
        CompletableFuture<Void> attrCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = this.productFeignClient.getAttrList(skuId);

            List<HashMap<String, Object>> attrListMap = attrList.stream().map(baseAttrInfo -> {
                HashMap<String, Object> map = new HashMap<>();
                String attrName = baseAttrInfo.getAttrName();
                //  不用循环：集合中只有一条数据！
                String valueName = baseAttrInfo.getAttrValueList().get(0).getValueName();

                map.put("attrName", attrName);
                map.put("attrValue", valueName);
                return map;
            }).collect(Collectors.toList());
            result.put("skuAttrList", attrListMap);
        }, threadPoolExecutor);

        //更新商品热度incrHotScore
        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);


        //  多任务组合：需要使用all:
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewCompletableFuture,
                priceCompletableFuture,
                spuSaleAttrCompletableFuture,
                spuPosterCompletableFuture,
                strJsonCompletableFuture,
                attrCompletableFuture,
                incrHotScoreCompletableFuture).join();

        //  返回数据.
        return result;
    }
}
