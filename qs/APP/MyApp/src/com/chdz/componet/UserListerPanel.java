package com.chdz.componet;



import com.chdz.global.AppRunTimeData;
import com.chdz.model.FriendInfo;
import com.chdz.network.ClientSocket;
import com.chdz.view.ChatMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// 好友面板 - 显示好友列表
public class UserListerPanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private JList<String> friendList;
    private DefaultListModel<String> friendModel;

    // 模拟的好友数据
    private List<Friend> friends = new ArrayList<>();

    public UserListerPanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
        ArrayList<FriendInfo> friendList = clientSocket.getFriendList();
        for (FriendInfo friend : friendList) {
            friends.add(new Friend(friend.getFriendName(), friend.getFriendId()));
        }
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        // 创建好友列表模型
        friendModel = new DefaultListModel<>();
        friendList = new JList<>(friendModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 添加双击事件监听，打开聊天框
        friendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 双击打开聊天框
                    int index = friendList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < friends.size()) {
                        Friend friend = friends.get(index);
                        parent.openChatWindow(friend.getName(), friend.getId());
                    }
                }
            }
        });

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(friendList);
        add(scrollPane, BorderLayout.CENTER);

        loadFriendList();
//        // 加载模拟好友数据
//        loadMockFriends();
    }

//    // 加载模拟好友数据
//    private void loadMockFriends() {
//        friends.add(new Friend("张三", "user001", "在线"));
//        friends.add(new Friend("李四", "user002", "在线"));
//        friends.add(new Friend("王五", "user003", "离线"));
//        friends.add(new Friend("赵六", "user004", "在线"));
//        friends.add(new Friend("钱七", "user005", "离线"));
//        friends.add(new Friend("孙八", "user006", "在线"));
//
//        // 更新列表显示
//        updateFriendList();
//    }

    private void loadFriendList() {

    }
    // 更新好友列表显示
    private void updateFriendList() {
        friendModel.clear();
        for (Friend friend : friends) {
            friendModel.addElement(friend.getName());
        }
    }

    // 好友数据类
    private class Friend {
        private String name;
        private String id;


        public Friend(String name, String ids) {
            this.name = name;
            this.id = id;

        }

        // Getters
        public String getName() { return name; }
        public String getId() { return id; }
    }
}
