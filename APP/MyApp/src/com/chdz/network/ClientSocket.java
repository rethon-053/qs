package com.chdz.network;

import com.chdz.componet.ChatWindow;
import com.chdz.componet.MessagePanel;
import com.chdz.componet.TrackPanel;
import com.chdz.componet.UserListerPanel;
import com.chdz.global.AppRunTimeData;
import com.chdz.model.*;
import com.chdz.view.AbstractAppView;
import com.chdz.view.ChatMainFrame;
import com.chdz.view.LoginFrame;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class ClientSocket {
    private Socket socket;
    private OutputStream outputStream;
    private boolean isConnected = false;
    private boolean isLogin = false;
    private ObjectOutputStream oos;
    private ChatMainFrame chatMainFrame; // 添加对ChatMainFrame的引用
    private MessagePanel messagePanel; // 添加对MessagePanel的引用
    private Map<String, ArrayList<ChatMessage>> chatMessagesMap;
    private HashSet<AddFriendRequest> addFriendRequests = new HashSet<>();
    private Map<String, READ_STATUS_UPDATE> readStatusUpdateMap;
    private TrackPanel trackPanel;
    private UserListerPanel userListerPanel;
    private ArrayList<Post> rcvPosts = new ArrayList<>();
    private ArrayList<Post> sendPosts = new ArrayList<>();



    public void setChatMainFrame(ChatMainFrame chatMainFrame) {
        this.chatMainFrame = chatMainFrame;
    }
    public void setMessagePanel(MessagePanel messagePanel) {
        this.messagePanel = messagePanel;
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
    public void sendMessage(Object message) throws IOException {
        if (!isConnected || outputStream == null) {
            notifyError("未连接到服务器");
            return;
        }
        
        try {
            oos.writeObject(message);
            oos.flush();
        } catch (IOException e) {
            notifyError("发送消息失败: " + e.getMessage());
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
                    if (obj instanceof User user){
                        AppRunTimeData.getInstance().setCurrentUser(user);
                        File file = new File(getClass().getResource("/src/res/info.l").getFile());
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(user);
                        }
                        continue;
                    }
                    if (obj instanceof FriendInfo f){
                        AppRunTimeData.getInstance().getCurrentUser().getFriends().put(f.getId(),f);
                    }
                    if (obj instanceof Map<?, ?>) {
                        Map<?, ?> map = (Map<?, ?>) obj;
                        if (map.containsKey("chat")) {
                            chatMessagesMap = (Map<String, ArrayList<ChatMessage>>) map.get("chat");
                            if (chatMessagesMap == null) {
                                chatMessagesMap = new HashMap<>();
                            }
                        } else if (map.containsKey("read")) {
                            readStatusUpdateMap = (Map<String, READ_STATUS_UPDATE>) map.get("read");
                            if (readStatusUpdateMap == null) {
                                readStatusUpdateMap = new HashMap<>();

                            }
                        } else if (map.containsKey("rcvPosts")){
                            rcvPosts = (ArrayList<Post>) map.get("rcvPosts");
                            if (rcvPosts == null) {
                                rcvPosts = new ArrayList<>();
                            }
                        } else if (map.containsKey("sendPosts")){
                            sendPosts = (ArrayList<Post>) map.get("sendPosts");
                            if (sendPosts == null) {
                                sendPosts = new ArrayList<>();
                            }
                        }
                    }
                    if (obj instanceof Post post) {
                        rcvPosts.add(post);
                        if (trackPanel != null) {
                           SwingUtilities.invokeLater(() -> {
                               trackPanel.updatePostList(rcvPosts);
                           }) ;
                        }
                    }

                    if (obj instanceof HashSet<?>) {
                        addFriendRequests = (HashSet<AddFriendRequest>) obj;
                        if (chatMainFrame != null) {
                            chatMainFrame.updateAddFriendRequests();
                        }
                    }
                    if (obj instanceof AddFriendRequest addFriendRequest){
                        System.out.println("addFriendRequest: " + addFriendRequest.getRequestId());
                        // 处理添加好友请求

                        addFriendRequests.add(addFriendRequest);
                        SwingUtilities.invokeLater(()->{
                            try {
                                chatMainFrame.updateAddFriendRequests();
                                userListerPanel.revalidate();
                                userListerPanel.repaint();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    if (obj instanceof String s) {
                        System.out.println("text: " + s);
                        if (s.equals("Login Success")) {
                            isLogin = true;
                            // 登录成功后，切换到聊天界面
                            ChatMainFrame mainFrame = new ChatMainFrame();
                            this.setChatMainFrame(mainFrame); // 设置引用
                            AppRunTimeData.getInstance().changeView(mainFrame);

                        } else if (s.equals("Account already login")) {
                            JOptionPane.showMessageDialog(null,"该账号已在别处登录","重复登录",
                                    JOptionPane.ERROR_MESSAGE);
                        }else if (s.equals("Login Failed")) {
                            JOptionPane.showMessageDialog(null, "登录失败，请检查账号密码", "错误", JOptionPane.ERROR_MESSAGE);
                        } else if (s.equals("Register Success")) {
                            JOptionPane.showMessageDialog(null, "注册成功！", "成功",
                                    JOptionPane.INFORMATION_MESSAGE);
                            // 注册成功后跳转到登录页面
                            AppRunTimeData.getInstance().changeView(new LoginFrame());
                        } else if (s.equals("Account Already Exists")) {
                            JOptionPane.showMessageDialog(null, "账号已存在，请重试！", "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (s.equals("Add Request Sent")) {
                            JOptionPane.showMessageDialog(null, "请求已发送！", "成功",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else if (s.equals("Add Request Failed")) {
                            JOptionPane.showMessageDialog(null, "该用户不存在！", "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (s.equals("Cannot add self")) {
                            JOptionPane.showMessageDialog(null, "不能添加自己！", "错误",
                                    JOptionPane.INFORMATION_MESSAGE);
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
                            SwingUtilities.invokeLater(() -> {
                                                                // 获取打开的聊天窗口并追加消息
                                Map<String, ChatWindow> chatWindows = getChatWindowsFromMainFrame();
                                if (chatWindows != null && chatWindows.containsKey(from)) {
                                    ChatWindow chatWindow = chatWindows.get(from);
                                    chatWindow.appendMessage(chat.getTime(),from, content);
                                }
                            });

                            SwingUtilities.invokeLater(() -> {
                                // 更新消息面板
                                if (messagePanel != null) {
                                   messagePanel.loadConversations();
                                       messagePanel.revalidate();
                                       messagePanel.repaint();
                                }
                            });
                        }
                    }

                }
            } catch (IOException e) {
                if (isConnected) { // 只在连接状态下报告错误
                    if (e.getMessage() == null) {
                        return;
                    }
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
            }
        }).start();
    }

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
        if (AppRunTimeData.getInstance().getCurrentUser() == null) {
            System.err.println("用户未登录，无法发送消息");
            return;
        }

        ChatMessage chatMsg = new ChatMessage(AppRunTimeData.getInstance().getCurrentUser().getAccount(), receiverId, content);
        sendMessage(chatMsg);
    }
    
    // 断开连接
    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;
        try {
            logout();
        } catch (IOException e) {
           e.printStackTrace();
        }
        try {
            Thread.sleep(1000); // 等待1000毫秒，确保消息发送完成
        } catch (InterruptedException e) {

        }

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
            sendMessage(new LogoutMessage(AppRunTimeData.getInstance().getCurrentUser().getAccount()));
        } catch (IOException e) {

        }
    }
    public Map<String, ArrayList<ChatMessage>> getChatMessagesMap() {
        return chatMessagesMap;
    }

    public void sendAddFriendRequest(String friendId) throws IOException {
        AddFriendRequest addFriendRequest = new AddFriendRequest(friendId,
                AppRunTimeData.getInstance().getCurrentUser().getAccount(),
                AppRunTimeData.getInstance().getCurrentUser().getName(),0,sendPosts,
                AppRunTimeData.getInstance().getCurrentUser().getAvatar()
        );
        sendMessage(addFriendRequest);
    }
    public HashSet<AddFriendRequest> getAddFriendRequests() {
        return addFriendRequests;
    }
    public void acceptFriendRequest(String friendId) throws IOException {
        AddFriendRequest addFriendRequest = new AddFriendRequest(friendId,
                AppRunTimeData.getInstance().getCurrentUser().getAccount(),
                AppRunTimeData.getInstance().getCurrentUser().getName(),1,sendPosts,
                AppRunTimeData.getInstance().getCurrentUser().getAvatar()
        );
        sendMessage(addFriendRequest);
    }
    public void rejectFriendRequest(String friendId) throws IOException {
        AddFriendRequest addFriendRequest = new AddFriendRequest(friendId,
                AppRunTimeData.getInstance().getCurrentUser().getAccount(),
                AppRunTimeData.getInstance().getCurrentUser().getName(),2,null,
                AppRunTimeData.getInstance().getCurrentUser().getAvatar()
        );
        sendMessage(addFriendRequest);
    }
    public void updateUser() throws IOException {
        sendMessage(AppRunTimeData.getInstance().getCurrentUser());
    }
    public void sendCompleteRequest(AddFriendRequest addFriendRequest) throws IOException {
        addFriendRequest.setStatus(3);
        addFriendRequest.setPosts(sendPosts);
        sendMessage(addFriendRequest);
    }

    public READ_STATUS_UPDATE getReadStatusUpdate(String friendId) {
        return readStatusUpdateMap.getOrDefault(friendId+"_"+AppRunTimeData.getInstance().getCurrentUser().getAccount(), null);
    }
    public void sendRSU(READ_STATUS_UPDATE rsu) throws IOException {
        sendMessage(rsu);
    }
    public Map<String, READ_STATUS_UPDATE> getReadStatusUpdateMap() {
        return readStatusUpdateMap;
    }
    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public void sendPost(Post post) throws IOException {
        sendPosts.add(post);
        sendMessage(post);
    }

    public void setTrackPanel(TrackPanel trackPanel) {
        this.trackPanel = trackPanel;
    }
    public ArrayList<Post> getRcvPosts() {
        return rcvPosts;
    }
    public ArrayList<Post> getSendPosts() {
        return sendPosts;
    }

    public void sendUpdateUserInfo(User u) throws IOException {
        HashMap<String,User> updateUser = new HashMap<>();
        updateUser.put("updateInfo", u);
        sendMessage(updateUser);
    }
    public TrackPanel getTrackPanel() {
        return trackPanel;
    }

    public void setUserListerPanel(UserListerPanel userListerPanel) {
        this.userListerPanel = userListerPanel;
    }
}