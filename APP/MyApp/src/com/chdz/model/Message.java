package com.chdz.model;

import java.io.Serializable;

// 消息基类
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    

    private String senderId;
    private long timestamp;
    
    public Message(String senderId) {
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSenderId() {
        return senderId;
    }
    @Override
    public String toString() {
        return "Message{senderId='" + senderId + "'}";
    }
    public long getTime() {
        return timestamp;
    }
}

