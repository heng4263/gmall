package com.atguigu.gmall.gateway.filter;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.util.List;


@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    // 匹配路径的工具类
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取到请求对象
        ServerHttpRequest request = exchange.getRequest();
        // 获取Url
        String path = request.getURI().getPath();
        // 如果是内部接口，则网关拦截不允许外部访问！
        if (antPathMatcher.match("/**/inner/**", path)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 获取用户Id
        String userId = getUserId(request);
        //  获取临时用户Id
        String userTempId = this.getUserTempId(request);
        //token被盗用
        if ("-1".equals(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        // 用户登录认证
        //api接口，异步请求，校验用户必须登录
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        // 验证url
        for (String authUrl : authUrls.split(",")) {
            // 当前的url包含登录的控制器域名，但是用户Id 为空！
            if (path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                //303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                // 重定向到登录
                return response.setComplete();
            }
        }
        // 将userId传递给后端
        //  难一点的，就要面向对象. 工具.
        //  校验完成之后，将用户Id 存储到请求头
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)) {
            //  判断登录用户Id
            if (!org.springframework.util.StringUtils.isEmpty(userId)) {
                //  如何添加 header。 ServerHttpRequest request
                //  将request封装header. ServerHttpRequest。
                request.mutate().header("userId", userId).build();
            }
            //  判断未登录用户Id
            if (!org.springframework.util.StringUtils.isEmpty(userTempId)) {
                //  如何添加 header。 ServerHttpRequest request
                //  将request封装header. ServerHttpRequest。
                request.mutate().header("userTempId", userTempId).build();
            }
            //  request 将带有请求头的对象 转换为 ---> exchange
            chain.filter(exchange.mutate().request(request).build());
        }

        //  默认过滤成功.
        return chain.filter(exchange);
    }


    /**
     * 获取当前登录用户id
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if (null != tokenList) {
            token = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("token");
            if (cookie != null) {
                token = URLDecoder.decode(cookie.getValue());
            }
        }
        if (!StringUtils.isEmpty(token)) {
            String userStr = (String) redisTemplate.opsForValue().get("user:login:" + token);
            JSONObject userJson = JSONObject.parseObject(userStr);
            String ip = userJson.getString("ip");
            String curIp = IpUtil.getGatwayIpAddress(request);
            //校验token是否被盗用
            if (ip.equals(curIp)) {
                return userJson.getString("userId");
            } else {
                //ip不一致
                return "-1";
            }
        }
        return "";
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //  信息提示：
        //  String message = resultCodeEnum.getMessage(); // 非法请求.
        //  ResultCodeEnum 封装到 Result 中
        Result<Object> result = Result.build(null, resultCodeEnum);
        //  result ---> 变为字符串.
        String str = JSONObject.toJSONString(result);
        //  找到输出方法.
        DataBuffer wrap = response.bufferFactory().wrap(str.getBytes());
        //  设置字符集
        response.getHeaders().set("Content-Type", "application/json;charset=UTF-8");
        //  返回数据.
        return response.writeWith(Mono.just(wrap));
    }

    /**
     * 获取当前用户临时用户id
     * @param request
     * @return
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId = "";
        List<String> tokenList = request.getHeaders().get("userTempId");
        if (null != tokenList) {
            userTempId = tokenList.get(0);
        } else {
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("userTempId");
            if (cookie != null) {
                userTempId = URLDecoder.decode(cookie.getValue());
            }
        }
        return userTempId;
    }

}
