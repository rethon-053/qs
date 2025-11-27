package com.chdz.model;

import java.io.Serializable;

public class READ_STATUS_UPDATE implements Serializable {
     private static final long serialVersionUID = 1L;
     private String senderId;
     private String receiverId;
     private long sendTime;
     public READ_STATUS_UPDATE(String senderId, String receiverId, long sendTime) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.sendTime = sendTime;
    }
     public String getSenderId() { return senderId; }
     public String getReceiverId() { return receiverId; }
     public long getSendTime() { return sendTime; }
}
