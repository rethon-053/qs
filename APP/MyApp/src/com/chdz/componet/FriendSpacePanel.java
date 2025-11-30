package com.chdz.componet;

import javax.swing.*;


import com.chdz.Util.AvatarUtil;
import com.chdz.model.Post;
import com.chdz.model.FriendInfo;
import com.chdz.view.ChatMainFrame;
import com.chdz.global.AppRunTimeData;
import com.chdz.network.ClientSocket;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

// 好友空间面板 - 通过继承复用TrackPanel的功能
public class FriendSpacePanel extends TrackPanel {
    private ChatMainFrame parent;
    private FriendInfo friendInfo;
    private JButton backButton;

    public FriendSpacePanel(ChatMainFrame parent) {
        super(parent); // 调用父类构造函数
        this.parent = parent;
        customizeUI(); // 自定义UI元素
    }

    private void customizeUI() {
        // 移除父类的发布按钮
        Component[] components = getComponents();
        if (components.length > 0 && components[0] instanceof JPanel) {
            JPanel topPanel = (JPanel) components[0];
            topPanel.removeAll();

            // 添加返回按钮
            backButton = new JButton("返回好友信息");
            topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            if (friendInfo != null) {
                JLabel avatarLabel = AvatarUtil.createAvatarLabel(
                        friendInfo.getAvatar(),
                        AvatarUtil.USER_AVATAR_SIZE
                );
                topPanel.add(avatarLabel);

                JLabel nameLabel = new JLabel(friendInfo.getName());
                nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
                topPanel.add(nameLabel);
            }

            topPanel.add(Box.createHorizontalStrut(20));
            topPanel.add(backButton);

            // 设置返回按钮事件
            backButton.addActionListener(e -> {
                if (friendInfo != null) {
                    parent.showFriendInfoPanel(friendInfo);
                }
            });

            topPanel.revalidate();
            topPanel.repaint();
        }
    }

    // 重写此方法以显示特定好友的动态
    public void setFriendInfo(FriendInfo friendInfo) {
        this.friendInfo = friendInfo;
        customizeUI();

        // 过滤只显示该好友的动态
        ClientSocket clientSocket = AppRunTimeData.getInstance().getClientSocket();
        ArrayList<Post> allPosts = clientSocket.getRcvPosts();
        ArrayList<Post> friendPosts = allPosts.stream()
                .filter(post -> post.getSenderId().equals(friendInfo.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        // 更新标题
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.setTitle(friendInfo.getName() + "的空间");
        }

        // 使用父类的方法加载过滤后的动态
        updatePostList(friendPosts);
    }

    // 重写显示空状态的方法，显示好友特定的空状态信息
    @Override
    public void showEmptyState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(500, 200));
        emptyPanel.setLayout(new BorderLayout());

        JLabel emptyLabel = new JLabel(
                friendInfo != null ? friendInfo.getName() + "暂无动态" : "暂无动态",
                SwingConstants.CENTER
        );
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setVerticalAlignment(SwingConstants.CENTER);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);

        // 获取父类的postsDisplayPanel
        Component[] components = getComponents();
        if (components.length > 1 && components[1] instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) components[1];
            Component view = scrollPane.getViewport().getView();
            if (view instanceof JPanel) {
                JPanel postsDisplayPanel = (JPanel) view;
                postsDisplayPanel.removeAll();
                postsDisplayPanel.add(emptyPanel);
                postsDisplayPanel.revalidate();
                postsDisplayPanel.repaint();
            }
        }
    }
}
