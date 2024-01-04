package org.jeecg.modules.system.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitRequest {
    long time() default 1; // 限制时间 单位：分钟
    int count() default Integer.MAX_VALUE; // 允许请求的次数
}