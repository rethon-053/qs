package com.chdz.componet;
import com.chdz.Util.AvatarUtil;
import com.chdz.global.AppRunTimeData;
import com.chdz.model.ChatMessage;
import com.chdz.model.FriendInfo;
import com.chdz.model.READ_STATUS_UPDATE;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private String currentUserAccount = AppRunTimeData.getInstance().getCurrentUser().getAccount();

    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;

    public ChatWindow(String friendName, String friendId, ClientSocket clientSocket) {
        this.friendName = friendName;
        this.friendId = friendId;
        this.clientSocket = clientSocket;
        init();
        ArrayList<ChatMessage> chatMessages = clientSocket.getChatMessagesMap().getOrDefault(friendId, new ArrayList<>());
        for (ChatMessage chat : chatMessages) {
            appendMessage(chat.getTime(), friendId.equals(chat.getFrom()) ?  friendId : "我", chat.getContent());
        }
        if (!chatMessages.isEmpty()) {
            clientSocket.getReadStatusUpdateMap().put(friendId + "_" + currentUserAccount, new READ_STATUS_UPDATE(friendId, currentUserAccount, chatMessages.get(chatMessages.size() - 1).getTime()));
            try {
                clientSocket.sendRSU(new READ_STATUS_UPDATE(friendId, currentUserAccount, chatMessages.get(chatMessages.size() - 1).getTime()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void init() {
        setTitle("与 " + friendName + " 聊天中");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 创建主面板，使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建聊天内容区域
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



        // 添加滚动面板
        scrollPane = new JScrollPane(chatPanel);
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


        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                SwingUtilities.invokeLater(() -> {
                    try {
                        clientSocket.getReadStatusUpdateMap().put(friendId + "_" + currentUserAccount, new READ_STATUS_UPDATE(friendId, currentUserAccount, System.currentTimeMillis()));
                        clientSocket.sendRSU(new READ_STATUS_UPDATE(friendId, currentUserAccount, System.currentTimeMillis()));

                        if (clientSocket.getMessagePanel() != null) {
                            clientSocket.getMessagePanel().loadConversations();
                            clientSocket.getMessagePanel().revalidate();
                            clientSocket.getMessagePanel().repaint();
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    dispose();
                });
            }
        });
    }

    // 发送消息
    private void sendMessage() throws IOException {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示自己发送的消息
            appendMessage(System.currentTimeMillis(),"我", message);
            ChatMessage chatMessage = new ChatMessage(AppRunTimeData.getInstance().getCurrentUser().getAccount(), friendId, message);
            clientSocket.getChatMessagesMap().getOrDefault(friendId,new ArrayList<>()).add(chatMessage);
            clientSocket.sendRSU(new READ_STATUS_UPDATE(friendId, currentUserAccount, chatMessage.getTime()));
            clientSocket.getReadStatusUpdateMap().put(friendId + "_" + currentUserAccount, new READ_STATUS_UPDATE(friendId, currentUserAccount, chatMessage.getTime()));
            // 这里应该添加通过ClientSocket发送消息到服务器的代码
            // clientSocket.sendMessageToUser(friendId, message);
            clientSocket.sendChat(friendId, message);

            // 清空输入框
            inputField.setText("");
        }
    }



    // 追加消息到聊天区域
    public void appendMessage(long time,String sender, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(new Date(time));

        JPanel messageItem = new JPanel();

        byte[] avatarData = null;
        if (!sender.equals("我")) {
            avatarData = AppRunTimeData.getInstance().getCurrentUser().getFriends().get(sender).getAvatar();
        }
        byte[] myAvatarData = AppRunTimeData.getInstance().getCurrentUser().getAvatar();

        if (sender.equals("我")){
            messageItem.setLayout(new FlowLayout(FlowLayout.RIGHT));

            JPanel contentPanel = new JPanel();
            contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            contentPanel.setBackground(new Color(100, 180, 255));

            JLabel contentLabel = new JLabel(message);
            contentLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            contentPanel.add(contentLabel);
            contentPanel.setForeground(Color.WHITE);
            JLabel avatarLabel = AvatarUtil.createAvatarLabel(myAvatarData, AvatarUtil.CHAT_AVATAR_SIZE);

            messageItem.add(contentPanel);
            messageItem.add(avatarLabel);
        } else {
            messageItem.setLayout(new FlowLayout(FlowLayout.LEFT));

            JLabel avatarLabel = AvatarUtil.createAvatarLabel(avatarData, AvatarUtil.CHAT_AVATAR_SIZE);

            JPanel contentPanel = new JPanel();
            contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            contentPanel.setBackground(new Color(227, 230, 235));

            JLabel contentLabel = new JLabel(message);
            contentLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            contentPanel.add(contentLabel);
            contentPanel.setForeground(new Color(15, 15, 15));

            messageItem.add(avatarLabel);
            messageItem.add(contentPanel);
        }
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        chatPanel.add(timeLabel);
        chatPanel.add(messageItem);
        chatPanel.add(Box.createVerticalStrut(10));

        this.revalidate();
        this.repaint();
        SwingUtilities.invokeLater(()-> {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
    }
}