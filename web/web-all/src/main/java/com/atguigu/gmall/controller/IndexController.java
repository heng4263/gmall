package com.atguigu.gmall.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.IOException;

@Controller
public class IndexController {
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ProductFeignClient productFeignClient;
    //  http://www.gmall.com/index.html 或 http://www.gmall.com/ 都可以访问首页！
    //  int [] arrys = {2,3,4,5}; String args [] = {"2","4","8"}

    // 第一种缓存渲染方式：
    @GetMapping({"/", "index.html"})
    public String index(HttpServletRequest request) {
        // 获取首页分类数据    ${list}
        Result result = productFeignClient.getBaseCategoryList();
        //  后台将数据 赋值给result.data = Result.ok(categoryList);
        //  model.addAllAttributes()
        request.setAttribute("list", result.getData());
        return "index/index";
    }

    //第二种缓存渲染方式:nginx做静态代理方式
    //  控制器---获取静态化页面！
    @GetMapping("createIndex")
    @ResponseBody
    public Result crateIndex(){
        //  获取数据：
        Result result = productFeignClient.getBaseCategoryList();
        //  模板引擎：
        //  设置页面显示的内容.
        Context context = new Context();
        //  给模板页面的key 赋值！
        context.setVariable("list",result.getData());
        //  Writer
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("D:\\index.html");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //  void process(String var1, IContext var2, Writer var3);
        templateEngine.process("index/index.html",context,fileWriter);
        //  默认返回：
        return Result.ok();
    }
}
