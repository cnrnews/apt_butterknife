package com.wangyi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 作用在属性上
@Target(ElementType.METHOD)
// 编译期
@Retention(RetentionPolicy.CLASS)
public @interface OnClick {
    int value();
}