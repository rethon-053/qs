package com.chdz.model;

import java.io.Serializable;

// 消息基类
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String senderId;
    private long timestamp;
    
    public Message(MessageType type, String senderId) {
        this.type = type;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "Message{type=" + type + ", senderId='" + senderId + "'}";
    }
    public long getTime() {
        return timestamp;
    }
}

