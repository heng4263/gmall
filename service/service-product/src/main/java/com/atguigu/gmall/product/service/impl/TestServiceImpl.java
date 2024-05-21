package com.atguigu.gmall.product.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Override
//    public void testLock() {
//        //  设置uuId
//        String uuid = UUID.randomUUID().toString();
//        //  缓存的lock 对应的值 ，应该是index2 的uuid
//        Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 1, TimeUnit.SECONDS);
//        //  判断flag index=1
//        if (flag) {
//            //  说明上锁成功！ 执行业务逻辑
//            String value = redisTemplate.opsForValue().get("num");
//            //  判断
//            if (StringUtils.isEmpty(value)) {
//                return;
//            }
//            //  进行数据转换
//            int num = Integer.parseInt(value);
//            //  放入缓存
//            redisTemplate.opsForValue().set("num", String.valueOf(++num));
//
//            //  定义一个lua 脚本
//            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//
//            //  准备执行lua 脚本
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//            //  将lua脚本放入DefaultRedisScript 对象中
//            redisScript.setScriptText(script);
//            //  设置DefaultRedisScript 这个对象的泛型
//            redisScript.setResultType(Long.class);
//            //  执行删除
//            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
//
//        } else {
//            //  没有获取到锁！
//            try {
//                Thread.sleep(1000);
//                //  睡醒了之后，重试
//                testLock();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void testLock() {
        // 创建锁：
        String skuId = "25";
        String locKey = "lock:" + skuId;
        // 锁的是每个商品
        RLock lock = redissonClient.getLock(locKey);
        // 开始加锁
        lock.lock();
        // 业务逻辑代码
        // 获取数据
        String value = redisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(value)) {
            return;
        }
        // 将value 变为int
        int num = Integer.parseInt(value);
        // 将num +1 放入缓存
        redisTemplate.opsForValue().set("num", String.valueOf(++num));
        // 解锁：
        lock.unlock();
    }

    @Override
    public String readLock() {
        // 初始化读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readWriteLock.readLock(); // 获取读锁

        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁

        String msg = this.redisTemplate.opsForValue().get("msg");

        //rLock.unlock(); // 解锁
        return msg;
    }

    @Override
    public String writeLock() {
        // 初始化读写锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
        RLock rLock = readWriteLock.writeLock(); // 获取写锁

        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁

        this.redisTemplate.opsForValue().set("msg", UUID.randomUUID().toString());

        //rLock.unlock(); // 解锁
        return "成功写入了内容。。。。。。";
    }


}

