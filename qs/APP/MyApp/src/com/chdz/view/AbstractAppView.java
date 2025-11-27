package com.chdz.view;

import com.chdz.global.AppRunTimeData;
import com.chdz.global.Const;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class AbstractAppView extends JPanel {
    /**
     * APP窗体
     */
    protected static JFrame frame;

    static {
        frame = new JFrame();
        frame.setTitle("测试");
        frame.setSize(Const.WIDTH, Const.HEIGHT + Const.TITLE_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 获取当前视图状态
                ViewStatus status = AppRunTimeData.getInstance().getCurrView().getStatus();
                // 当前视图逻辑空转
                AppRunTimeData.getInstance().getCurrView().status = ViewStatus.NONE;
                // 显示退出提示
                int choose = JOptionPane.showConfirmDialog(frame, "是否退出应用？", "提示", JOptionPane.YES_NO_OPTION);
                if (choose == JOptionPane.YES_OPTION) {
                    frame.dispose();
                    if (AppRunTimeData.getInstance().getClientSocket().isLogin()) {
                        try {
                            AppRunTimeData.getInstance().getClientSocket().logout();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    System.out.println("程序已退出");
                    System.exit(0);
                }
                // 恢复视图状态
                AppRunTimeData.getInstance().getCurrView().status = status;
            }
        });
        frame.setVisible(true);
    }

    /**
     * 构造函数设置一些默认选项
     */
    protected AbstractAppView() {
        // 设置内容面板的首选大小（不包含窗口装饰）
        setPreferredSize(new Dimension(Const.WIDTH, Const.HEIGHT));
    }

    //必须重写初始化方法
    protected abstract void init();

    /**
     * 视图状态
     */
    protected ViewStatus status = ViewStatus.ON_ENTER;

    /**
     * 要切换下一个视图
     */
    protected AbstractAppView nextView;

    /**
     * 绘制视图，处理动态渲染逻辑
     * @param dlt 时间间隔
     */
    protected abstract void draw(long dlt);

    /**
     * 通用切换视图事件函数，方便给按钮绑定事件
     * @param view 要切换的视图
     */
    protected void changeView(AbstractAppView view) {
        this.nextView = view;
        this.status = ViewStatus.ON_EXIT;
    }

    /**
     * 获取视图状态
     * @return 视图状态
     */
    public ViewStatus getStatus() {
        return status;
    }

    // 新增：初始化标志，防止重复 init
    private boolean initialized = false;

    /**
     * 进入视图，默认调用init函数（但只第一次调用）
     */
    public void onEnter() {
        // 只在首次进入时初始化一次
        if (!initialized) {
            init();
            initialized = true;
        }
        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        // 显示窗口
        frame.setVisible(true);
        // 下一帧进入更新状态
        status = ViewStatus.ON_UPDATE;
    }

    /**
     * 更新视图，默认调用draw和handleInput
     * @param dlt 时间间隔
     */
    public void onUpdate(long dlt) {
        // 处理用户输入
        //handleInput();
        // 绘制视图
        draw(dlt);
    }

    /**
     * 退出视图，用于处理退出视图后执行的逻辑
     */
    public void onExit() {
        // 设置为空转状态
        status = ViewStatus.NONE;
        // 默认页面切换逻辑
        if (nextView != null) {
            frame.remove(this);
            AppRunTimeData.getInstance().changeView(nextView);
        }
    }
}