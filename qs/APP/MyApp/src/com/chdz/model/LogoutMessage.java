package com.chdz.model;

public class LogoutMessage extends Message {
    private String userId;
    public LogoutMessage(String userId) {
        super(MessageType.LOGOUT, userId);
        this.userId = userId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
