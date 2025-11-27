package com.chdz.componet;



import com.chdz.global.AppRunTimeData;
import com.chdz.model.AddFriendRequest;
import com.chdz.model.FriendInfo;
import com.chdz.network.ClientSocket;
import com.chdz.view.ChatMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 好友面板 - 显示好友列表
public class UserListerPanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private JList<String> friendList;
    private DefaultListModel<String> friendModel;
    private JButton addFriend;
    private JList<AddFriendRequest> addFriendRequestList = new JList<>();
    private DefaultListModel<AddFriendRequest> addFriendRequestModel;
    private JPanel requestContainer = new JPanel();


    private List<FriendInfo> friends = new ArrayList<>();
    private ArrayList<AddFriendRequest> addFriendRequests = new ArrayList<>();
    public UserListerPanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        requestContainer = new JPanel();
        requestContainer.setLayout(new BoxLayout(requestContainer, BoxLayout.Y_AXIS));
        add(new JScrollPane(requestContainer), BorderLayout.SOUTH);


        // 添加好友按钮
        setupAddButton();
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
                        FriendInfo f = friends.get(index);
                        parent.openChatWindow(f.getName(), f.getId());
                    }
                }
            }
        });
               // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(friendList);
        add(scrollPane, BorderLayout.CENTER);

        loadFriendList();
        loadAddFriendRequests();


//        // 加载模拟好友数据
//        loadMockFriends();
    }

    private void setupAddButton() {
        addFriend = new JButton("添加好友");
        addFriend.setBounds(270, 15, 100, 30);
        addFriend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 显示添加好友对话框
                try {
                    showAddFriendDialog();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.add(addFriend);
    }

    private void loadFriendList() {
        updateFriendList();
    }
    private void loadAddFriendRequests() {
        addFriendRequests = new ArrayList<>(clientSocket.getAddFriendRequests());
    }
    // 更新好友列表显示
    private void updateFriendList() {
        friends = AppRunTimeData.getInstance().getCurrentUser().getFriends();
        friendModel.clear();
        if (friends == null) {
            return;
        }
        for (FriendInfo friend : friends) {
            friendModel.addElement(friend.getName());
        }
    }

    public void updateAddFriendRequests() throws IOException {
        requestContainer.removeAll();
        addFriendRequests = new ArrayList<>(clientSocket.getAddFriendRequests());
        for (AddFriendRequest addFriendRequest : addFriendRequests) {
            if (addFriendRequest.getStatus() == 0) {
                requestContainer.add(new AddFriendRequestItem(addFriendRequest, this, clientSocket));
            } else if (addFriendRequest.getStatus() == 1) {
                AppRunTimeData.getInstance().getCurrentUser().addFriend(new FriendInfo(addFriendRequest.getName(), addFriendRequest.getRequestId()));
                updateFriendList();
                clientSocket.updateUser();
                clientSocket.sendCompleteRequest(addFriendRequest);
            }
       }
        requestContainer.revalidate();
        requestContainer.repaint();
    }

    // 显示添加好友对话框
    private void showAddFriendDialog() throws IOException {
        String friendId = JOptionPane.showInputDialog(this, "请输入好友ID:", "添加好友", JOptionPane.PLAIN_MESSAGE);
        if (friendId != null && !friendId.trim().isEmpty()) {
            // 这里可以添加发送添加好友请求的逻辑
            clientSocket.sendAddFriendRequest(friendId);
        }
    }

    public void acceptRequest(AddFriendRequest addFriendRequest) {
        try {
            clientSocket.acceptFriendRequest(addFriendRequest.getRequestId());
            clientSocket.getAddFriendRequests().remove(addFriendRequest);
            updateAddFriendRequests();
            AppRunTimeData.getInstance().getCurrentUser().getFriends().add(new FriendInfo(addFriendRequest.getName(), addFriendRequest.getRequestId()));
            updateFriendList();
            clientSocket.updateUser();
            JOptionPane.showMessageDialog(this, "已同意: " + addFriendRequest.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void rejectRequest(AddFriendRequest addFriendRequest) {
        try {
            clientSocket.rejectFriendRequest(addFriendRequest.getRequestId());
            JOptionPane.showMessageDialog(this, "已拒绝: " + addFriendRequest.getName());
            clientSocket.getAddFriendRequests().remove(addFriendRequest);
            updateAddFriendRequests();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
