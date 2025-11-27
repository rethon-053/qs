package com.chdz.network;

import com.chdz.model.Message;
import com.chdz.model.ChatMessage;
import com.chdz.model.LoginMessage;
import com.chdz.model.RegisterMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageHandler {
    private ClientSocket clientSocket;
    private String currentUserId; // 当前登录用户ID
    
    public MessageHandler(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;

        // 设置消息接收处理器
    }



    



}