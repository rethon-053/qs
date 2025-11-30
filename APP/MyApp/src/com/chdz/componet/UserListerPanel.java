package com.chdz.componet;



import com.chdz.App;
import com.chdz.Util.AvatarUtil;
import com.chdz.global.AppRunTimeData;
import com.chdz.model.AddFriendRequest;
import com.chdz.model.ChatMessage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 好友面板 - 显示好友列表
public class UserListerPanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private JPanel friendListPanel;
    private JButton addFriend;
    private JPanel requestContainer = new JPanel();


    private Map<String,FriendInfo> friends = new HashMap<>();
    private ArrayList<AddFriendRequest> addFriendRequests = new ArrayList<>();
    public UserListerPanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();

        init();
        clientSocket.setUserListerPanel(this);
    }

    private void init() {
        setLayout(new BorderLayout());

        requestContainer = new JPanel();
        requestContainer.setLayout(new BoxLayout(requestContainer, BoxLayout.Y_AXIS));
        add(new JScrollPane(requestContainer), BorderLayout.SOUTH);


        // 添加好友按钮
        setupAddButton();
        // 创建好友列表模型
        friendListPanel = new JPanel();
        friendListPanel.setLayout(new BoxLayout(friendListPanel, BoxLayout.Y_AXIS));


               // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(friendListPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadFriendList();
        loadAddFriendRequests();


    }

    private void setupAddButton() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addFriend = new JButton("添加好友");

        addFriend.setPreferredSize(new Dimension(100, 30));

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
        buttonPanel.add(addFriend);
        this.add(buttonPanel, BorderLayout.NORTH);
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
        friendListPanel.removeAll();
        if (friends == null) {
            return;
        }
        for (FriendInfo friend : friends.values()) {
            JPanel friendItem = createFriendItem(friend);
            friendListPanel.add(friendItem);
            friendListPanel.add(Box.createVerticalStrut(2));
        }
        friendListPanel.revalidate();
        friendListPanel.repaint();
    }
    private JPanel createFriendItem(FriendInfo friend) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        item.setMinimumSize(new Dimension(0, 50));
        item.setPreferredSize(new Dimension(0, 50));

        JLabel avatarLabel = AvatarUtil.createAvatarLabel(
                friend.getAvatar(),
                AvatarUtil.CHAT_AVATAR_SIZE
        );
        item.add(avatarLabel);

        JLabel nameLabel = new JLabel(friend.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        item.add(nameLabel);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 双击打开聊天框
                    parent.showFriendInfoPanel(friend);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(Color.LIGHT_GRAY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(null);
            }
        });
        return item;
    }

    public void updateAddFriendRequests() throws IOException {
        requestContainer.removeAll();
        addFriendRequests = new ArrayList<>(clientSocket.getAddFriendRequests());
        for (AddFriendRequest addFriendRequest : addFriendRequests) {
            if (addFriendRequest.getStatus() == 0) {
                requestContainer.add(new AddFriendRequestItem(addFriendRequest, this, clientSocket));
            } else if (addFriendRequest.getStatus() == 1) {
                AppRunTimeData.getInstance().getCurrentUser().getFriends().put(
                                addFriendRequest.getRequestId(), new FriendInfo(
                                addFriendRequest.getName(),
                                addFriendRequest.getRequestId(),
                                addFriendRequest.getAvatarData())
                );
                updateFriendList();
                clientSocket.updateUser();
                SwingUtilities.invokeLater(()->{
                    clientSocket.getRcvPosts().addAll(addFriendRequest.getPosts());
                    if (clientSocket.getTrackPanel() != null) {
                        clientSocket.getTrackPanel().updatePostList(clientSocket.getRcvPosts());
                        clientSocket.getTrackPanel().revalidate();
                        clientSocket.getTrackPanel().repaint();
                    }
                });

                clientSocket.sendCompleteRequest(addFriendRequest);
            } else if (addFriendRequest.getStatus() == 3) {
                SwingUtilities.invokeLater(()->{
                    clientSocket.getRcvPosts().addAll(addFriendRequest.getPosts());
                    if (clientSocket.getTrackPanel() != null) {
                        clientSocket.getTrackPanel().updatePostList(clientSocket.getRcvPosts());
                        clientSocket.getTrackPanel().revalidate();
                        clientSocket.getTrackPanel().repaint();
                    }
                });
            }
       }
        requestContainer.revalidate();
        requestContainer.repaint();
    }

    // 显示添加好友对话框
    private void showAddFriendDialog() throws IOException {
        String friendId = JOptionPane.showInputDialog(this,
                "请输入好友ID:", "添加好友", JOptionPane.PLAIN_MESSAGE);
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
            AppRunTimeData.getInstance().getCurrentUser().getFriends()
                    .put(addFriendRequest.getRequestId(),
                            new FriendInfo(addFriendRequest.getName(),
                                addFriendRequest.getRequestId(),
                                addFriendRequest.getAvatarData()));
            updateFriendList();
            clientSocket.updateUser();
            ChatMessage hello = new ChatMessage(addFriendRequest.getFriendId(),
                    addFriendRequest.getRequestId(),
                    "我已经同意了你的好友申请,现在可以开始聊天啦!");
            clientSocket.sendMessage(hello);
            clientSocket.getChatMessagesMap().put(addFriendRequest.getRequestId(), new ArrayList<>());
            clientSocket.getChatMessagesMap().get(addFriendRequest.getRequestId()).add(hello);

            SwingUtilities.invokeLater(()->{
                if (clientSocket.getMessagePanel() != null) {
                    clientSocket.getMessagePanel().loadConversations();
                }
            });
            clientSocket.getRcvPosts().addAll(addFriendRequest.getPosts());
            SwingUtilities.invokeLater(()->{
                if (clientSocket.getTrackPanel() != null) {
                    clientSocket.getTrackPanel().updatePostList(clientSocket.getRcvPosts());
                    clientSocket.getTrackPanel().revalidate();
                    clientSocket.getTrackPanel().repaint();
                }
            });

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
