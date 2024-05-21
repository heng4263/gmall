package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        /**
         * 核心线程数
         * 拥有最多线程数
         * 表示空闲线程的存活时间
         * 存活时间单位
         * 用于缓存任务的阻塞队列
         * 省略：
         *  threadFactory：指定创建线程的工厂
         *  handler：表示当workQueue已满，且池中的线程数达到maximumPoolSize时，线程池拒绝添加新任务时采取的策略。
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5, //  核心线程 io:2*n  cpu: n+1;
                100, // 最大线程数
                3, // 空闲线程存活时间
                TimeUnit.SECONDS,// 时间单位
                new ArrayBlockingQueue<>(3) // 阻塞队列
                //                Executors.defaultThreadFactory(),
                //                new ThreadPoolExecutor.AbortPolicy() // 抛出异常.
        );
        //  当前这个线程池能够处理最大的任务数是几? 13
        return threadPoolExecutor;
    }
}
