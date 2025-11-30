package com.chdz.model;

import java.io.Serializable;

public class FriendInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String friendName;
    private String friendId;
    private byte[] friendAvatar;

    public FriendInfo(String friendName, String friendId, byte[] friendAvatar) {
        this.friendName = friendName;
        this.friendId = friendId;
        this.friendAvatar = friendAvatar;
    }
    public byte[] getAvatar() {
        return friendAvatar;
    }
    public String getName() {
        return friendName;
    }

    public String getId() {
        return friendId;
    }
}
