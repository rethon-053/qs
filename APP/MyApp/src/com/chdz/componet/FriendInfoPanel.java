package com.chdz.componet;

import com.chdz.Util.AvatarUtil;
import com.chdz.global.AppRunTimeData;
import com.chdz.model.FriendInfo;
import com.chdz.view.ChatMainFrame;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;

public class FriendInfoPanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private FriendInfo friendInfo;
    private JButton backButton;
    private JButton messageButton;
    private JButton spaceButton;

    public FriendInfoPanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
        init();
    }

    private void init() {
        setLayout(null); // 使用绝对布局便于精确控制按钮位置
        setBackground(Color.WHITE);
    }

    // 设置要显示的好友信息
    public void setFriendInfo(FriendInfo friendInfo) {
        this.friendInfo = friendInfo;
        updatePanel();
    }

    // 更新面板内容
    private void updatePanel() {
        // 清除现有组件
        removeAll();

        // 返回按钮 - 左上角
        backButton = new JButton("返回");
        backButton.setBounds(20, 20, 80, 30);
        backButton.addActionListener(e -> parent.showFriendPanel()); // 返回好友列表
        add(backButton);

        // 好友头像
        JPanel avatarPanel = new JPanel();
        avatarPanel.setBounds(170, 80, 80, 80);
        avatarPanel.setBackground(Color.WHITE);
        JLabel avatar = AvatarUtil.createAvatarLabel(
                friendInfo.getAvatar(),
                AvatarUtil.SHOW_AVATAR_SIZE
        );
        avatarPanel.add(avatar);
        add(avatarPanel);

        // 好友名称
        JLabel nameLabel = new JLabel(friendInfo.getName());
        nameLabel.setFont(new Font("宋体", Font.BOLD, 18));
        nameLabel.setBounds(160, 180, 120, 30);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(nameLabel);

        // 好友ID
        JLabel idLabel = new JLabel("ID: " + friendInfo.getId());
        idLabel.setFont(new Font("宋体", Font.PLAIN, 14));
        idLabel.setForeground(Color.GRAY);
        idLabel.setBounds(160, 220, 120, 20);
        idLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(idLabel);

        // 发消息按钮 - 正下方
        messageButton = new JButton("发消息");
        messageButton.setBounds(100, 320, 120, 40);
        messageButton.addActionListener(e -> {
            try {
                // 先返回好友面板，再打开聊天窗口
                parent.showFriendPanel();
                parent.openChatWindow(friendInfo.getName(), friendInfo.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        add(messageButton);

        // 查看空间按钮
        spaceButton = new JButton("查看空间");
        spaceButton.setBounds(240, 320, 120, 40);
        spaceButton.addActionListener(e -> {
            // 这里可以实现查看好友空间的功能
            parent.showFriendSpacePanel(friendInfo);
        });
        add(spaceButton);

        // 刷新面板
        revalidate();
        repaint();
    }
}