package com.chdz.controller;

import com.chdz.global.AppRunTimeData;
import com.chdz.network.ClientSocket;
import com.chdz.network.MessageHandler;
import com.chdz.view.AbstractAppView;
import com.chdz.view.LoginFrame;
import com.chdz.view.RegisterFrame;
import org.w3c.dom.views.AbstractView;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.chdz.global.Const.DLT;

public class AppCore {
    /**
     * 线程池：程序运行过程中不阻塞的线程池
     */
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1, r -> {
        Thread t = new Thread(r, "gls");
        t.setDaemon(false);
        return t;
    });

    /**
     * 游戏设置：设置程序运行过程中不变动的设置
     */
    private void setup() {


    }

    /**
     * 游戏结束：程序结束对应资源处理
     */
    private void end() {


        // 销毁线程池资源
        scheduler.shutdown();

        // 执行程序退出
        System.out.println("程序退出");
        System.exit(0);
    }

    /**
     * 程序逻辑主心跳：游戏逻辑主心跳
     * @param dlt 时间间隔
     */
    private void task(long dlt) {
        // 记录一个开始时间
        long start = System.currentTimeMillis();

        // 程序
        AppRunTimeData runTimeData = AppRunTimeData.getInstance();
        if (runTimeData.isExit) {
            // 结束资源释放
            end();
            return;
        }

        // 获取当前视图
        AbstractAppView cv = runTimeData.getCurrView();
        // 根据状态执行不同的逻辑
        switch (cv.getStatus()) {
            case ON_ENTER:
                cv.onEnter();
                break;
            case ON_UPDATE:
                cv.onUpdate(dlt);
                break;
            case ON_EXIT:
                cv.onExit();
                break;
            default:
                // 空转逻辑
                break;
        }

        // 计算逻辑执行时间
        long end = System.currentTimeMillis();
        long cost = end - start;

        // 开启下一帧逻辑
        scheduler.schedule(() -> task(Math.max(cost, DLT)), Math.max(DLT - cost, 0), TimeUnit.MILLISECONDS);
    }

    /**
     * 游戏运行：游戏运行
     */
    public void run() {
        // 设置APP
        setup();

        ClientSocket cs = new ClientSocket();
//        cs.connect("uei3wlmhh6.localto.net", 80);
        cs.connect("localhost", 8888);
        if (!cs.isConnected()) {
            //连接失败直接结束程序
            System.err.println("连接服务器失败");
            return;
        }

        AppRunTimeData.getInstance().setClientSocket(cs);
        // 设置第一个视图
        AppRunTimeData.getInstance().changeView(new LoginFrame());

        // 执行程序逻辑主心跳
        scheduler.schedule(() -> task(0), 0, TimeUnit.MILLISECONDS);
    }
}
