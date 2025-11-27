package com.chdz.controller;

import com.chdz.Util.UserManager;
import com.chdz.global.AppRunTimeData;
import com.chdz.network.ClientSocket;
import com.chdz.view.LoginFrame;

import java.io.IOException;


//登录控制器  与LoginJFrame相互绑定
public class LoginController {
    private LoginFrame loginJFrame;
    private ClientSocket clientSocket;

    // 构造函数，绑定登录界面和消息处理程序
    public LoginController(LoginFrame loginJFrame, ClientSocket clientSocket) {
        UserManager.loadData();
        this.loginJFrame = loginJFrame;
        this.clientSocket = clientSocket;
    }
    /**
     * 登录按钮点击事件
     */
    public void loginButtonClick() throws IOException {
        // 从登录界面获取用户名和密码
        String account = loginJFrame.getAccount();
        String password = loginJFrame.getPassword();

        clientSocket.sendLogin(account, password);
    }
}
