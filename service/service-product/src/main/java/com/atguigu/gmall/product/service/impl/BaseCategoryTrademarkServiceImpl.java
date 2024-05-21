package com.atguigu.gmall.product.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {

    //  调用mapper 层！
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        //  根据分类Id 获取到品牌Id 集合数据
        //  select * from base_category_trademark where category3_id = ?;
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        //  判断baseCategoryTrademarkList 这个集合
        if(!CollectionUtils.isEmpty(baseCategoryTrademarkList)){
            //  需要获取到这个集合中的品牌Id 集合数据
            List<Long> tradeMarkIdList = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {
                return baseCategoryTrademark.getTrademarkId();
            }).collect(Collectors.toList());
            //  正常查询数据的话... 需要根据品牌Id 来获取集合数据！
            return baseTrademarkMapper.selectBatchIds(tradeMarkIdList);
        }
        //  如果集合为空，则默认返回空
        return null;
    }

    @Override
    public void removeBaseCategoryTrademarkById(Long category3Id, Long trademarkId) {
        //  逻辑删除： 本质更新操作 is_deleted
        //  更新： update base_category_trademark set is_deleted = 1 where category3_id=? and trademark_id=?;
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id",category3Id);
        baseCategoryTrademarkQueryWrapper.eq("trademark_id",trademarkId);
        baseCategoryTrademarkMapper.delete(baseCategoryTrademarkQueryWrapper);

    }

    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        //  哪些是关联的品牌Id
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        //  判断
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)){
            //  找到关联的品牌Id 集合数据 {1,3}
            List<Long> tradeMarkIdList = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {
                return baseCategoryTrademark.getTrademarkId();
            }).collect(Collectors.toList());
            //  在所有的品牌Id 中将这些有关联的品牌Id 给过滤掉就可以！
            //  select * from base_trademark; 外面 baseTrademarkMapper.selectList(null) {1,2,3,5}
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null).stream().filter(baseTrademark -> {
                return !tradeMarkIdList.contains(baseTrademark.getId());
            }).collect(Collectors.toList());
            //  返回数据
            return baseTrademarkList;
        }
        //  如果说这个三级分类Id 下 没有任何品牌！ 则获取到所有的品牌数据！
        return baseTrademarkMapper.selectList(null);
    }



    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        /*
        private Long category3Id;
        private List<Long> trademarkIdList;

        category3Id 61 tmId 2;
        category3Id 61 tmId 5;
         */
        //  获取到品牌Id 集合数据
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();

        //  判断
        if (!CollectionUtils.isEmpty(trademarkIdList)){
                  //  做映射关系
            List<BaseCategoryTrademark> baseCategoryTrademarkList = trademarkIdList.stream().map((trademarkId) -> {
                //  创建一个分类Id 与品牌的关联的对象
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                baseCategoryTrademark.setTrademarkId(trademarkId);
                //  返回数据
                return baseCategoryTrademark;
            }).collect(Collectors.toList());

            //  集中保存到数据库    baseCategoryTrademarkList
            this.saveBatch(baseCategoryTrademarkList);
        }
    }
}
