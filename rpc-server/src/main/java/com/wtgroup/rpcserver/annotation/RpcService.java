package com.wtgroup.rpcserver.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 标注注解的生命周期：RUNTIME表示在运行时可以被获取到
@Retention(RetentionPolicy.RUNTIME)
// 标注注解的使用范围：TYPE代表应用于类、接口（包括注解）、枚举
@Target(ElementType.TYPE)
@Component
public @interface RpcService {

    Class<?> serviceInterface() default Object.class;

    String serviceVersion() default "1.0";
}
