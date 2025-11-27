package com.chdz.model;

// 聊天消息
public class ChatMessage extends Message {
    private String from;
    private String to;
    private String content;
    private boolean read;

    public String getFrom() { return from; }
    public String getTo() { return to; }


    public ChatMessage(String senderId, String receiverId, String content) {
        super(MessageType.CHAT, senderId);
        this.from = senderId;
        this.to = receiverId;
        this.content = content;

    }
     public boolean isRead() { return read; }
     public void setRead(boolean read) { this.read = read; }
    
    public String getContent() { return content; }
}
