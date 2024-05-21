package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD}) // 使用的范围 TYPE 类上 METHOD 方法上
@Retention(RetentionPolicy.RUNTIME) // 注解的生命周期：RUNTIME
@Inherited
@Documented
public @interface GmallCache {

    //  定义一个锁的前缀：
    String prefix() default "cache:";
    //  定义一个锁的后缀：
    String suffix() default ":info";

}
