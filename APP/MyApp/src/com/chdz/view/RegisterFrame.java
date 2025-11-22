package com.chdz.view;

import com.chdz.global.Const;

import javax.swing.*;
import java.awt.*;

//注册界面
public class RegisterFrame extends AbstractAppView {
    @Override
    protected void init() {
        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        frame.setTitle("注册界面");
        setLayout(null);

        //如果涉及到页面跳转
        //  调用这个方法即可 changeView("要跳转的页面");
    }
    @Override
    protected void draw(long dlt) {

    }
}

