package com.atguigu.gmall.common.cache;

import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheImpl {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheImpl(ProceedingJoinPoint joinPoint){
        //  声明对象
        Object obj = new Object();
        /*
        实现分布式锁的业务：
            查询缓存的数据：
                true: 返回
                false:  分布式锁 查询数据库 返回放入redis
         */

        //  必须先组成缓存的key！
        //  获取参数
        Object[] args = joinPoint.getArgs();
        //  获取到注解的前缀，后缀
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        String prefix = gmallCache.prefix();
        String suffix = gmallCache.suffix();
        String key = prefix + Arrays.asList(args) + suffix;

        //  当前方法的返回值类型。
        //  Class returnType = methodSignature.getReturnType();
        try {
            //  从缓存中获取数据.
            obj = redisTemplate.opsForValue().get(key);
            //  判断
            if (obj==null){
                //  缓存中么有数据.
                //  组成分布式锁的key 有几个方法参数相同：
                String locKey = prefix + Arrays.asList(args)+":lock";
                RLock lock = this.redissonClient.getLock(locKey);
                //  上锁
                lock.lock();
                try {
                    //  查询数据库：本质执行带有注解的方法体
                    obj = joinPoint.proceed(args);
                    //  判断数据
                    if (obj==null){
                        Object o = new Object();
                        //  存储到缓存.
                        this.redisTemplate.opsForValue().setIfAbsent(key,o, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        //  返回数据
                        return o;
                    }else {
                        this.redisTemplate.opsForValue().setIfAbsent(key,obj, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        //  返回数据
                        return obj;
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                } finally {
                    //  释放锁
                    lock.unlock();
                }
            } else {
                //  缓存中有数据。
                return obj;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        //  返回对象
        return joinPoint.proceed(args);
    }
}
