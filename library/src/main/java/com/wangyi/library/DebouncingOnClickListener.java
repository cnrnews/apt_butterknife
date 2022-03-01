package com.wangyi.library;

import android.view.View;

/**
 * @Author lihl
 * @Date 2022/2/27 16:53
 * @Email 1601796593@qq.com
 * 点击监听接口，实现类（抽象类 + 抽象方法）
 */
public abstract class DebouncingOnClickListener implements View.OnClickListener{
    @Override
    public void onClick(View v) {
        // 调用抽象方法
        doClick(v);
    }
    public abstract void doClick(View view);
}
