package com.atguigu.gmall.product.controller;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //  自定义线程池： 工具类创建的线程池：要么是阻塞队列，要么是最大线程个数 都是Integer.MAX_VALUE 因为会导致OOM!
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                5,
                3,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3)
        );

        //  并行：
        CompletableFuture<String> completableFutureA = CompletableFuture.supplyAsync(() -> "hello");


        //  线程B 获取到A 的结果。
        CompletableFuture<Void> completableFutureB = completableFutureA.thenAcceptAsync(s -> {
            //  s = hello
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(s + ":\t B");
        }, threadPoolExecutor);

        //  线程C 获取到A 的结果。
        CompletableFuture<Void> completableFutureC = completableFutureA.thenAcceptAsync(s -> {
            //  s = hello
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(s + ":\t C");
        }, threadPoolExecutor);

        System.out.println(completableFutureB.get());
        System.out.println(completableFutureC.get());

    }

    //串行：
//                CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
//                    //  异步执行线程！
//                    System.out.println("我是有返回值的线程");
//                      int i = 1/0;
//                    return 1024;
//                }).thenApply(f->{
//                    System.out.println("f:\t"+f);
//                    return f*2;
//                }).whenComplete((t,u)->{
//                    System.out.println("t:\t"+t);
//                    System.out.println("u:\t"+u);
//                }).exceptionally(throwable -> {
//                    System.out.println("throwable:\t"+throwable);
//                    return 404;
//                });
//
//                System.out.println(integerCompletableFuture.get());
//
//    }


//        CompletableFuture future = CompletableFuture.supplyAsync(new Supplier<Object>() {
//            //supplyAsync 可以返回值
//            @Override
//            public Object get() {
//                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
//                int i = 10 / 0;
//                return 1024;
//            }
//        }).whenComplete(new BiConsumer<Object, Throwable>() {
//            @Override
//            public void accept(Object o, Throwable throwable) {
//                System.out.println("-------o=" + o.toString());
//                System.out.println("-------throwable=" + throwable);
//            }
//        }).exceptionally(new Function<Throwable, Object>() {
//            @Override
//            public Object apply(Throwable throwable) {
//                System.out.println("throwable=" + throwable);
//                return 6666;
//            }
//        });
//        System.out.println(future.get());
//    }
}
