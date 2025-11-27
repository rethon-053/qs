package com.chdz.network;

import com.chdz.componet.ChatWindow;
import com.chdz.global.AppRunTimeData;
import com.chdz.model.*;
import com.chdz.view.AbstractAppView;
import com.chdz.view.ChatMainFrame;
import com.chdz.view.LoginFrame;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientSocket {
    private Socket socket;
    private OutputStream outputStream;
    private ArrayList<FriendInfo> friendList;
    private boolean isConnected = false;
    private boolean isLogin = false;
    private ObjectOutputStream oos;
    private ChatMainFrame chatMainFrame; // 添加对ChatMainFrame的引用
    private Map<String, ArrayList<ChatMessage>> chatMessagesMap;


    public void setChatMainFrame(ChatMainFrame chatMainFrame) {
        this.chatMainFrame = chatMainFrame;
    }

    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }
    public boolean isLogin() {
        return isLogin;
    }
    
    // 获取Socket
    public Socket getSocket() {
        return socket;
    }

    // 连接到服务器
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            outputStream = socket.getOutputStream();
            isConnected = true;
            oos = new ObjectOutputStream(outputStream);
            // 启动接收消息的线程
            startReceiveThread();
            return true;
        } catch (IOException e) {
            notifyError("连接失败: " + e.getMessage());
            return false;
        }
    }
    
    // 发送消息
    public void sendMessage(Message message) throws IOException {
        if (!isConnected || outputStream == null) {
            notifyError("未连接到服务器");
            return;
        }
        
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            notifyError("发送消息失败: " + e.getMessage());
            disconnect();
        }
    }
    
    // 启动接收消息的线程
    private void startReceiveThread() {
        new Thread(() -> {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(socket.getInputStream());

                while (isConnected) {
                    Object obj = ois.readObject();

                    if (obj instanceof Map<?, ?>) {
                        chatMessagesMap = (Map<String, ArrayList<ChatMessage>>) obj;
                    }
                    if (obj instanceof ArrayList<?>) {
                        ArrayList<?> list = (ArrayList<?>) obj;
                        if (list.get(0) instanceof FriendInfo) {
                            friendList = (ArrayList<FriendInfo>) list;
                        }
                    }
                    if (obj instanceof TextMessage) {
                        TextMessage text = (TextMessage) obj;
                        System.out.println("text: " + text.getText());
                        if (text.getText().equals("Login Success")) {
                            isLogin = true;
                            // 登录成功后，切换到聊天界面
                            ChatMainFrame mainFrame = new ChatMainFrame();
                            this.setChatMainFrame(mainFrame); // 设置引用
                            AppRunTimeData.getInstance().changeView(mainFrame);
                        } else if (text.getText().equals("Account already login")) {
                            AppRunTimeData.getInstance().setCurrUserId(null);
                            JOptionPane.showMessageDialog(null,"该账号已在别处登录","重复登录",
                                    JOptionPane.ERROR_MESSAGE);
                        }else if (text.getText().equals("Login Failed")) {
                            AppRunTimeData.getInstance().setCurrUserId(null);
                            JOptionPane.showMessageDialog(null, "登录失败，请检查账号密码", "错误", JOptionPane.ERROR_MESSAGE);
                        } else if (text.getText().equals("Register Success")) {
                            JOptionPane.showMessageDialog(null, "注册成功！", "成功",
                                    JOptionPane.INFORMATION_MESSAGE);
                            // 注册成功后跳转到登录页面
                            AppRunTimeData.getInstance().changeView(new LoginFrame());
                        } else if (text.getText().equals("Account Already Exists")) {
                            JOptionPane.showMessageDialog(null, "账号已存在，请重试！", "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    if (obj instanceof ChatMessage chat) {
                        String from = chat.getFrom();
                        String content = chat.getContent();
                        // 检查是否有聊天记录
                        ArrayList<ChatMessage> messages = chatMessagesMap.getOrDefault(from, new ArrayList<>());
                        messages.add(chat);
                        chatMessagesMap.put(from, messages);

                        // 新的消息处理逻辑：找到对应的聊天窗口
                        if (chatMainFrame != null) {
                            // 通过friendId查找或创建聊天窗口
                            String friendName = from; // 如果有好友名称映射，可以使用实际名称
                            
                            // 调用ChatMainFrame的方法来打开或获取聊天窗口
                            SwingUtilities.invokeLater(() -> {
                                chatMainFrame.openChatWindow(friendName, from);
                                
                                // 获取打开的聊天窗口并追加消息
                                Map<String, ChatWindow> chatWindows = getChatWindowsFromMainFrame();
                                if (chatWindows != null && chatWindows.containsKey(from)) {
                                    ChatWindow chatWindow = chatWindows.get(from);
                                    chatWindow.appendMessage(from, content);
                                }
                            });
                        }
                    }

                }
            } catch (IOException e) {
                if (isConnected) { // 只在连接状态下报告错误
                    notifyError("接收消息错误: " + e.getMessage());
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        notifyError("刷新输入流失败: " + e.getMessage());
                    }
                }
                disconnect();
            }
        }).start();
    }
    
    // 获取ChatMainFrame中的聊天窗口映射
    private Map<String, ChatWindow> getChatWindowsFromMainFrame() {
        try {
            // 使用反射获取ChatMainFrame中的chatWindows字段
            java.lang.reflect.Field field = ChatMainFrame.class.getDeclaredField("chatWindows");
            field.setAccessible(true);
            return (Map<String, ChatWindow>) field.get(chatMainFrame);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 登录界面调用此方法
    public void sendLogin(String account, String password) throws IOException {
        LoginMessage loginMsg = new LoginMessage(account, password);
        sendMessage(loginMsg);
    }

    // 注册界面调用此方法
    public void sendRegister(String nickname, String account, String password, String email) throws IOException {
        RegisterMessage registerMsg = new RegisterMessage(nickname, account, password, email);
        sendMessage(registerMsg);
    }

    // 聊天界面调用此方法
    public void sendChat(String receiverId, String content) throws IOException {
        if (AppRunTimeData.getInstance().getCurrUserId() == null) {
            System.err.println("用户未登录，无法发送消息");
            return;
        }

        ChatMessage chatMsg = new ChatMessage(AppRunTimeData.getInstance().getCurrUserId(), receiverId, content);
        sendMessage(chatMsg);
    }
    
    // 断开连接
    public void disconnect() {
        if (!isConnected) return;
        
        isConnected = false;
        try {
            if (socket != null) socket.close();
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            // 忽略关闭时的错误
        }
    }

    // 通知错误
    private void notifyError(String error) {
        System.err.println(error);
    }
    
    // 获取连接状态
    public boolean isConnected() {
        return isConnected;
    }
    public void logout() throws IOException {
        try {
            sendMessage(new LogoutMessage(AppRunTimeData.getInstance().getCurrUserId()));
        } catch (IOException e) {
            throw new IOException("注销失败: " + e.getMessage(), e);
        }
    }
    public Map<String, ArrayList<ChatMessage>> getChatMessagesMap() {
        return chatMessagesMap;
    }
    public ArrayList<FriendInfo> getFriendList() {
        return friendList;
    }
}