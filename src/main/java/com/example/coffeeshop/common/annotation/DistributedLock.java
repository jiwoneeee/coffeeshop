package com.example.coffeeshop.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key();           // 락 키 (SpEL 지원)
    long waitTime() default 5;    // 락 대기 시간 (초)
    long leaseTime() default 3;   // 락 점유 시간 (초)
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}