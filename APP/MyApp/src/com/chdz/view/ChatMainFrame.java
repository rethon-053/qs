package com.chdz.view;

import com.chdz.Util.AvatarUtil;
import com.chdz.componet.*;
import com.chdz.global.AppRunTimeData;
import com.chdz.global.Const;
import com.chdz.model.AddFriendRequest;
import com.chdz.model.FriendInfo;
import com.chdz.model.User;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

//聊天主界面
public class ChatMainFrame extends AbstractAppView {
    private JButton mess;
    private JButton friend;
    private JButton track;
    private ClientSocket clientSocket;
    private JLabel topAvatarLabel;
    private JLabel topNameLabel;
    
    // 使用CardLayout管理不同视图
    private CardLayout cardLayout;
    private JPanel mainPanel; // 主内容区域面板
    
    // 四个主要面板
    private MessagePanel chatPanel;
    private UserListerPanel friendPanel;
    private TrackPanel trackPanel;
    private FriendInfoPanel friendInfoPanel;
    private FriendSpacePanel friendSpacePanel;

    
    // 存储打开的聊天框
    private Map<String, ChatWindow> chatWindows = new HashMap<>();

    public ChatMainFrame() {
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
    }
    
    @Override
    protected void init() {
        setLayout(new BorderLayout()); // 使用BorderLayout管理整体布局

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setPreferredSize(new Dimension(Const.WIDTH, 60));

        topAvatarLabel = AvatarUtil.createAvatarLabel(
                AppRunTimeData.getInstance().getCurrentUser().getAvatar(),
                AvatarUtil.USER_AVATAR_SIZE
        );

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem openSpaceItem = new JMenuItem("打开个人空间");
        JMenuItem editProfileItem = new JMenuItem("编辑个人信息");

        openSpaceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPersonalSpace();
            }
        });

        editProfileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openEditProfilePanel();
            }
        });

        popupMenu.add(openSpaceItem);
        popupMenu.add(editProfileItem);

        topAvatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                popupMenu.show(topAvatarLabel, e.getX(), e.getY());
            }
        });

        topPanel.add(topAvatarLabel);

        topNameLabel = new JLabel(
                AppRunTimeData.getInstance().getCurrentUser().getName()
        );
        topNameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        topPanel.add(topNameLabel);

        // 创建主内容区域，使用CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // 初始化四个主要面板
        chatPanel = new MessagePanel(this);
        friendPanel = new UserListerPanel(this);
        trackPanel = new TrackPanel(this);
        clientSocket.setTrackPanel(trackPanel);
        friendInfoPanel = new FriendInfoPanel(this);
        friendSpacePanel = new FriendSpacePanel(this);
        
        // 将面板添加到主内容区域
        mainPanel.add(chatPanel, "chat");
        mainPanel.add(friendPanel, "friend");
        mainPanel.add(trackPanel, "track");
        mainPanel.add(friendInfoPanel, "friendInfo");
        mainPanel.add(friendSpacePanel, "friendSpace");

        
        // 创建底部按钮面板
        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(Const.WIDTH, 60));
        bottomPanel.setLayout(null);
        
        createButtons(bottomPanel);
        
        // 添加顶部信息面板、主内容区域和底部按钮面板
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        // 设置标题
        frame.setTitle("聊天界面");
        
        // 默认显示聊天面板
        chatPanel.loadConversations();
        showChatPanel();
    }

    private void openEditProfilePanel() {
        User u = AppRunTimeData.getInstance().getCurrentUser();

        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel mainpanel = new JPanel();
        mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));

        JPanel avatarPanel = new JPanel(new BorderLayout());

        JLabel avatarLabel = AvatarUtil.createAvatarLabel(
                AppRunTimeData.getInstance().getCurrentUser().getAvatar(),
                AvatarUtil.SHOW_AVATAR_SIZE
        );
        avatarPanel.add(avatarLabel);

        JButton uploadButton = new JButton("上传头像");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);

                chooser.setFileFilter(new FileNameExtensionFilter(
                        "图片文件", "jpg", "png"
                ));
                int result = chooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File selectedFile = chooser.getSelectedFile();
                        byte[] imageData = Files.readAllBytes(selectedFile.toPath());

                        avatarLabel.setIcon(AvatarUtil.createAvatarLabel(imageData, AvatarUtil.SHOW_AVATAR_SIZE).getIcon());
                        avatarLabel.revalidate();
                        avatarLabel.repaint();

                        avatarPanel.putClientProperty("newAvatar", imageData);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame,
                                "图片读取错误" + ex.getMessage(),
                                "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        avatarPanel.add(uploadButton, BorderLayout.SOUTH);

        JPanel formPanel = new JPanel(new GridLayout(3,2,10,10));
        JLabel nameLabel = new JLabel("用户名:");
        JTextField nameField = new JTextField(u.getName());

        JLabel passLabel = new JLabel("密码:");
        JPasswordField passField = new JPasswordField(u.getPassword());

        JLabel emailLabel = new JLabel("邮箱:");
        JTextField emailField = new JTextField(u.getEmail());

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passLabel);
        formPanel.add(passField);

        mainpanel.add(avatarPanel);
        mainpanel.add(Box.createVerticalStrut(20));
        mainpanel.add(formPanel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("返回");

        saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    u.setName(nameField.getText());
                    u.setEmail(emailField.getText());
                    u.setPassword(new String(passField.getPassword()));

                    byte[] newAvatar = (byte[]) avatarPanel.getClientProperty("newAvatar");
                    if (newAvatar != null) {
                        u.setAvatar(newAvatar);
                    }
                    AppRunTimeData.getInstance().setCurrentUser(u);
                    topNameLabel.setText(u.getName());
                    topAvatarLabel.setIcon(AvatarUtil.createAvatarLabel(newAvatar, AvatarUtil.USER_AVATAR_SIZE).getIcon());
                    topAvatarLabel.revalidate();
                    topAvatarLabel.repaint();
                    showChatPanel();
                    try {
                        clientSocket.sendUpdateUserInfo(u);
                        JOptionPane.showMessageDialog(frame, "保存成功","成功", JOptionPane.INFORMATION_MESSAGE);
                        cardLayout.show(mainPanel, "chat");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame,
                                "保存失败" + ex.getMessage(),
                                "错误", JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
        cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardLayout.show(mainPanel, "chat");
                }
            });

        bottomPanel.add(saveButton);
        bottomPanel.add(cancelButton);

        JScrollPane scrollPane = new JScrollPane(mainpanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        editPanel.add(scrollPane, BorderLayout.CENTER);
        editPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(editPanel,"editProfile");

        cardLayout.show(mainPanel, "editProfile");
    }

    private void openPersonalSpace() {
        User u = AppRunTimeData.getInstance().getCurrentUser();
        FriendInfo f = new FriendInfo(u.getName(), u.getAccount(), u.getAvatar());
        showFriendSpacePanel(f);
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

    public void showFriendInfoPanel(FriendInfo friendInfo) {
        friendInfoPanel.setFriendInfo(friendInfo);
        cardLayout.show(mainPanel, "friendInfo");
        frame.setTitle("好友信息" + friendInfo.getName());
    }
    public void showFriendSpacePanel(FriendInfo friendInfo) {
        friendSpacePanel.setFriendInfo(friendInfo);
        cardLayout.show(mainPanel, "friendSpace");
        frame.setTitle("好友" + friendInfo.getName() + "的空间");
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