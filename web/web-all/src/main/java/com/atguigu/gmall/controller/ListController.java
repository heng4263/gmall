package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品列表接口
 * </p>
 *
 */
@Controller
public class ListController {
    //  http://list.gmall.com/list.html?category3Id=61&trademark=1:小米
    //  http://list.gmall.com/list.html?keyword=小米手机&trademark=1:小米
    //  OpenFeign 传递参数
    //  1. 具体的参数; 指定 (@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId)
    //  2. 传递的是对象呢; 参数是的对象的时候 应该先传递到请求体：以Json 格式存在！
    @Autowired
    private ListFeignClient listFeignClient;

    /**
     * 列表搜索
     * @param searchParam
     * @return
     */
    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        //  远程调用： listFeignClient.search(searchParam);  feign ;  service-list;
        Result<Map> result = this.listFeignClient.list(searchParam);
        model.addAllAttributes(result.getData());
        //  后台需要存储key searchParam trademarkParam{品牌面包屑}  urlParam{记录用户通过了那些条件进行检索} propsParamList{平台属性面包屑} orderMap{排序规则}
        //  urlParam{记录用户通过了那些条件进行检索}
        //  如何获取到url 参数！
        // 记录拼接url
        String urlParam = makeUrlParam(searchParam);
        //  trademarkParam 品牌面包屑： 品牌：品牌名称
        //处理品牌条件回显
        String trademarkParam = this.makeTrademark(searchParam.getTrademark());
        //  propsParamList 平台属性面包屑：集合 机身内存：128G 运行内存：8G
        //处理平台属性条件回显   这个泛型如何确定? 根据页面显示的内容确定！
        List<Map<String, String>> propsParamList = this.makeProps(searchParam.getProps());
        //处理排序     排序规则： orderMap.type 类型 1:综合 -- hotScore 类型 2:价格  orderMap.sort asc:升序，desc:降序
        Map<String, Object> orderMap = this.dealOrder(searchParam.getOrder());

        //  trademarkList {品牌列表}  attrsList{平台属性集合} goodsList{商品集合}  pageNo totalPages
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("trademarkParam", trademarkParam);
        model.addAttribute("propsParamList", propsParamList);
        model.addAttribute("orderMap", orderMap);
        return "list/index";
    }

    // 制作返回的url
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();
        // 判断关键字
        if (searchParam.getKeyword() != null) {
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        // 判断一级分类
        if (searchParam.getCategory1Id() != null) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        // 判断二级分类
        if (searchParam.getCategory2Id() != null) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        // 判断三级分类
        if (searchParam.getCategory3Id() != null) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        // 处理品牌
        if (searchParam.getTrademark() != null) {
            if (urlParam.length() > 0) {
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        // 判断平台属性值
        if (null != searchParam.getProps()) {
            for (String prop : searchParam.getProps()) {
                if (urlParam.length() > 0) {
                    urlParam.append("&props=").append(prop);
                }
            }
        }
        return "list.html?" + urlParam.toString();
    }

    /**
     * 处理品牌条件回显
     * @param trademark
     * @return
     */
    private String makeTrademark(String trademark) {
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = StringUtils.split(trademark, ":");
            if (split != null && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return "";
    }

    /**
     * 处理平台属性(面包屑)条件回显
     * @param props
     * @return
     */
// 处理平台属性
    private List<Map<String, String>> makeProps(String[] props) {
        List<Map<String, String>> list = new ArrayList<>();
        // 2:v:n
        if (props != null && props.length != 0) {
            //  props=23:8G:运行内存&props=24:256G:机身内存
            for (String prop : props) {
                String[] split = StringUtils.split(prop, ":");
                if (split != null && split.length == 3) {
                    // 声明一个map
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("attrId", split[0]);
                    map.put("attrValue", split[1]);
                    map.put("attrName", split[2]);
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * 处理排序
     * @param order
     * @return
     */
    private Map<String, Object> dealOrder(String order) {
        Map<String, Object> orderMap = new HashMap<>();
        //  order=1:desc order=2:desc order=1:asc order=2:asc
        if (!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                // 传递的哪个字段
                orderMap.put("type", split[0]);
                // 升序降序
                orderMap.put("sort", split[1]);
            }
        } else {
            orderMap.put("type", "1");
            orderMap.put("sort", "asc");
        }
        return orderMap;
    }

}
