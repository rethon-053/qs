package com.chdz.componet;

import com.chdz.global.AppRunTimeData;
import com.chdz.model.ChatMessage;
import com.chdz.network.ClientSocket;
import com.chdz.view.ChatMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 聊天面板 - 显示会话列表
public class MessagePanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private JList<String> conversationList;
    private DefaultListModel<String> conversationModel;

    // 模拟的会话数据
    private List<Conversation> conversations = new ArrayList<>();

    public MessagePanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        // 创建会话列表模型
        conversationModel = new DefaultListModel<>();
        conversationList = new JList<>(conversationModel);
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.setCellRenderer(new ConversationCellRenderer());

        // 添加点击事件监听
        conversationList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // 单击打开聊天框
                    int index = conversationList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < conversations.size()) {
                        Conversation conv = conversations.get(index);
                        parent.openChatWindow(conv.getFriendName(), conv.getFriendId());
                    }
                }
            }
        });

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(conversationList);
        add(scrollPane, BorderLayout.CENTER);

        loadConversations();
//        // 加载模拟会话数据
//        loadMockConversations();
    }

    // 加载模拟会话数据
//    private void loadMockConversations() {
//        conversations.add(new Conversation("张三", "user001", "你好，最近怎么样？", 2, System.currentTimeMillis() - 3600000));
//        conversations.add(new Conversation("李四", "user002", "明天有空一起吃饭吗？", 0, System.currentTimeMillis() - 7200000));
//        conversations.add(new Conversation("王五", "user003", "项目进展如何？", 1, System.currentTimeMillis() - 86400000));
//        conversations.add(new Conversation("赵六", "user004", "周末去打球吗？", 0, System.currentTimeMillis() - 172800000));
//
//        // 更新列表显示
//        updateConversationList();
//    }
    private void loadConversations() {
        conversations.clear();
        for (Map.Entry<String, ArrayList<ChatMessage>> entry : clientSocket.getChatMessagesMap().entrySet()) {
            String friendId = entry.getKey();
            ArrayList<ChatMessage> chatMessages = entry.getValue();
            if (!chatMessages.isEmpty()) {
                ChatMessage lastMessage = chatMessages.get(chatMessages.size() - 1);
                conversations.add(new Conversation(lastMessage.getFrom(), friendId, lastMessage.getContent(), getUnreadCount(chatMessages), lastMessage.getTime()));
            }
        }
    }

    // 更新会话列表显示
    private void updateConversationList() {
        conversationModel.clear();
        for (Conversation conv : conversations) {
            String displayText = conv.getFriendName() + " - " + conv.getLastMessage();
            if (conv.getUnreadCount() > 0) {
                displayText += " (" + conv.getUnreadCount() + ")";
            }
            conversationModel.addElement(displayText);
        }
    }

    // 会话数据类
    private class Conversation {
        private String friendName;
        private String friendId;
        private String lastMessage;
        private int unreadCount;
        private long lastMessageTime;

        public Conversation(String friendName, String friendId, String lastMessage, int unreadCount, long lastMessageTime) {
            this.friendName = friendName;
            this.friendId = friendId;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
            this.lastMessageTime = lastMessageTime;
        }

        // Getters
        public String getFriendName() { return friendName; }
        public String getFriendId() { return friendId; }
        public String getLastMessage() { return lastMessage; }
        public int getUnreadCount() { return unreadCount; }
        public long getLastMessageTime() { return lastMessageTime; }
    }

    // 自定义会话列表单元格渲染器
    private class ConversationCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (index >= 0 && index < conversations.size()) {
                Conversation conv = conversations.get(index);
                if (conv.getUnreadCount() > 0) {
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }

            return this;
        }
    }
    private int getUnreadCount(ArrayList<ChatMessage> chatMessages) {
        int l=0, r = chatMessages.size() - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (chatMessages.get(mid).isRead()) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }
        return chatMessages.size() - l;
    }
}

