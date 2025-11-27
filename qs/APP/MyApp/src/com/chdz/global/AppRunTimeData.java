package com.chdz.global;

import com.chdz.network.ClientSocket;
import com.chdz.view.AbstractAppView;
import org.w3c.dom.views.AbstractView;

public class AppRunTimeData {
    /**
     * 单例对象
     */
    private static final AppRunTimeData INSTANCE = new AppRunTimeData();

    private ClientSocket clientSocket;

    private String currUserId;
    /**
     * 当前游戏视图
     */
    private AbstractAppView currView;

    /**
     * 是否退出游戏
     */
    public boolean isExit;

    /**
     * 当前分数
     */
    public int score;

    /**
     * 私有构造方法
     */
    private AppRunTimeData() {
        isExit = false;
    }


    /**
     * 获取单例对象
     * @return 单例对象
     */
    public static AppRunTimeData getInstance() {
        return INSTANCE;
    }

    /**
     * 切换游戏视图
     * @param view 新游戏视图
     */
    public void changeView(AbstractAppView view) {
        currView = view;
    }

    /**
     * 获取当前游戏视图
     * @return 当前游戏视图
     */
    public AbstractAppView getCurrView() {
        return currView;
    }

    public ClientSocket getClientSocket() {
        return clientSocket;
    }
    public void setClientSocket(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public String getCurrUserId() {
        return currUserId;
    }
    public void setCurrUserId(String currUserId) {
        this.currUserId = currUserId;
    }

}
