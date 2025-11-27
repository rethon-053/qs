package com.chdz.model;

public class FriendInfo {
    private String friendName;
    private String friendId;

    public FriendInfo(String friendName, String friendId) {
        this.friendName = friendName;
        this.friendId = friendId;
    }
    public String getFriendName() {
        return friendName;
    }

    public String getFriendId() {
        return friendId;
    }
}
