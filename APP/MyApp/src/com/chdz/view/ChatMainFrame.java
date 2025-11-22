package com.chdz.view;
//聊天主界面
public class ChatMainFrame extends AbstractAppView {
    @Override
    protected void init() {
        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        // 设置标题
        frame.setTitle("聊天界面");
        setLayout(null);
    }

    @Override
    protected void draw(long dlt) {

    }
}
