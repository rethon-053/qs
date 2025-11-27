package com.chdz.view;

import com.chdz.componet.*;
import com.chdz.global.AppRunTimeData;
import com.chdz.global.Const;
import com.chdz.model.AddFriendRequest;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

//聊天主界面
public class ChatMainFrame extends AbstractAppView {
    private JButton mess;
    private JButton friend;
    private JButton track;
    private JButton addFriend;
    private ClientSocket clientSocket;
    
    // 使用CardLayout管理不同视图
    private CardLayout cardLayout;
    private JPanel mainPanel; // 主内容区域面板
    
    // 三个主要面板
    private MessagePanel chatPanel;
    private UserListerPanel friendPanel;
    private TrackPanel trackPanel;
    
    // 存储打开的聊天框
    private Map<String, ChatWindow> chatWindows = new HashMap<>();

    public ChatMainFrame() {
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
    }
    
    @Override
    protected void init() {
        setLayout(new BorderLayout()); // 使用BorderLayout管理整体布局
        
        // 创建主内容区域，使用CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // 初始化三个主要面板
        chatPanel = new MessagePanel(this);
        friendPanel = new UserListerPanel(this);
        trackPanel = new TrackPanel(this);
        
        // 将面板添加到主内容区域
        mainPanel.add(chatPanel, "chat");
        mainPanel.add(friendPanel, "friend");
        mainPanel.add(trackPanel, "track");
        
        // 创建底部按钮面板
        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(Const.WIDTH, 60));
        bottomPanel.setLayout(null);
        
        createButtons(bottomPanel);
        
        // 添加主内容区域和底部按钮面板
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        // 设置标题
        frame.setTitle("聊天界面");
        
        // 默认显示聊天面板
        showChatPanel();
    }

    private void createButtons(JPanel panel) {
        mess = new JButton("消息");
        mess.setBounds(20, 15, 80, 30);
        mess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showChatPanel();
            }
        });
        panel.add(mess);
        
        friend = new JButton("好友");
        friend.setBounds(170, 15, 80, 30);
        friend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFriendPanel();
            }
        });
        panel.add(friend);
        
        track = new JButton("动态");
        track.setBounds(320, 15, 80, 30);
        track.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTrackPanel();
            }
        });
        panel.add(track);
        

    }
    
    // 显示聊天面板
    public void showChatPanel() {
        cardLayout.show(mainPanel, "chat");
        frame.setTitle("聊天界面");
    }
    
    // 显示好友面板
    public void showFriendPanel() {
        cardLayout.show(mainPanel, "friend");
        frame.setTitle("好友列表");
    }
    
    // 显示动态面板
    public void showTrackPanel() {
        cardLayout.show(mainPanel, "track");
        frame.setTitle("动态");
    }
    
    // 打开聊天框
    public void openChatWindow(String friendName, String friendId) {
        // 检查是否已存在该好友的聊天框
        if (!chatWindows.containsKey(friendId)) {
            ChatWindow chatWindow = new ChatWindow(friendName, friendId, clientSocket);
            chatWindows.put(friendId, chatWindow);
            chatWindow.setVisible(true);
            
            // 添加窗口关闭监听器，从映射中移除
            chatWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    chatWindows.remove(friendId);
                }
            });
        } else {
            // 如果已存在，则显示并前置该窗口
            ChatWindow existingWindow = chatWindows.get(friendId);
            existingWindow.setVisible(true);
            existingWindow.toFront();
        }
        chatPanel.loadConversations();
    }
    


    @Override
    protected void draw(long dlt) {
        // 可以在这里添加动态绘制逻辑
    }

    public void updateAddFriendRequests() throws IOException {
        if (friendPanel != null) {
            friendPanel.updateAddFriendRequests();
        }
    }
}