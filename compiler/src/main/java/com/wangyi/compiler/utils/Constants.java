package com.wangyi.compiler.utils;

/**
 * @Author lihl
 * @Date 2022/2/27 17:01
 * @Email 1601796593@qq.com
 */
public class Constants {
    // 注解处理器支持的注解类型
    public static final String BINDVIEW_ANNOTATION_TYPE = "com.wangyi.annotation.BindView";
    public static final String ONCLICK_ANNOTATION_TYPE = "com.wangyi.annotation.OnClick";

    // 布局、控件绑定实现的接口
    public static final String VIEWBINDER = "com.wangyi.library.ViewBinder";

    public static final String CLICKLISTENER = "com.wangyi.library.DebouncingOnClickListener";

    public static final String VIEW = "android.view.View";

    // bind 方法名
    public static final String BIND_METHOD_NAME = "bind";

    // bind 方法参数名 target
    public static final String TARGET_PARAMETER_NAME = "target";

}
