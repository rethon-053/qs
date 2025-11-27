package com.chdz.model;

import java.io.Serializable;

public class FriendInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String friendName;
    private String friendId;

    public FriendInfo(String friendName, String friendId) {
        this.friendName = friendName;
        this.friendId = friendId;
    }
    public String getName() {
        return friendName;
    }

    public String getId() {
        return friendId;
    }
}
