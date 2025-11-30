package com.chdz.network;

public class MessageHandler {
    private ClientSocket clientSocket;
    private String currentUserId; // 当前登录用户ID
    
    public MessageHandler(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;

        // 设置消息接收处理器
    }
}