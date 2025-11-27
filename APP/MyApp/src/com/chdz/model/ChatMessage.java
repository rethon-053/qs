package com.chdz.model;

// 聊天消息
public class ChatMessage extends Message {
    private String from;
    private String to;
    private String content;
    private long time;

    public String getFrom() { return from; }
    public String getTo() { return to; }


    public ChatMessage(String senderId, String receiverId, String content) {
        super(senderId);
        this.from = senderId;
        this.to = receiverId;
        this.content = content;
        this.time = System.currentTimeMillis();
    }

    public String getContent() { return content; }
    public long getTime() { return time; }
}
