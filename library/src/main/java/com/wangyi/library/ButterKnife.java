package com.wangyi.library;

import android.app.Activity;

/**
 * @Author lihl
 * @Date 2022/2/27 16:54
 * @Email 1601796593@qq.com
 *
 * 核心类
 */
public class ButterKnife {

    public static void bind(Activity activity){
        // 拼接生成的类名
        String className = activity.getClass().getName() + "$ViewBinder";
        try {
            Class<?> viewBinderClass = Class.forName(className);
            // 实例化接口实现类
            ViewBinder binder = (ViewBinder) viewBinderClass.newInstance();
            // 调用接口方法
            binder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
