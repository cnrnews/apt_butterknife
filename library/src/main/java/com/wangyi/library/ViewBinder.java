package com.wangyi.library;

import android.app.Activity;

/**
 * @Author lihl
 * @Date 2022/2/27 16:56
 * @Email 1601796593@qq.com
 * 接口绑定类（所有注解处理器生的类，都需要实现该接口，= 接口实现类）
 */
public interface ViewBinder<T> {
    void bind(T target);
}
