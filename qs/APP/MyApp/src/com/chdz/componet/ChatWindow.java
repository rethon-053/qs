package com.chdz.componet;
import com.chdz.model.ChatMessage;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

// 聊天框窗口
public class ChatWindow extends JFrame {
    private String friendName;
    private String friendId;
    private ClientSocket clientSocket;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public ChatWindow(String friendName, String friendId, ClientSocket clientSocket) {
        this.friendName = friendName;
        this.friendId = friendId;
        this.clientSocket = clientSocket;
        ArrayList<ChatMessage> chatMessages = clientSocket.getChatMessagesMap().getOrDefault(friendId, new ArrayList<>());
        for (ChatMessage chat : chatMessages) {
            appendMessage(chat.getFrom(), chat.getContent());
        }

        init();
    }

    private void init() {
        setTitle("与 " + friendName + " 聊天中");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 创建主面板，使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建聊天内容区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(chatArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建底部输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("发送");

        // 添加发送按钮事件监听
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // 添加回车键发送消息
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setPreferredSize(new Dimension(600, 50));

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // 显示欢迎消息
        appendMessage("系统消息", "开始与 " + friendName + " 的对话");
    }

    // 发送消息
    private void sendMessage() throws IOException {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示自己发送的消息
            appendMessage("我", message);

            // 这里应该添加通过ClientSocket发送消息到服务器的代码
            // clientSocket.sendMessageToUser(friendId, message);
            clientSocket.sendChat(friendId, message);
            // 清空输入框
            inputField.setText("");
        }
    }



    // 追加消息到聊天区域
    public void appendMessage(String sender, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());

        String formattedMessage = "[" + time + "] " + sender + ": " + message + "\n";
        chatArea.append(formattedMessage);

        // 自动滚动到底部
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
