package com.chdz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class AddFriendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String friendId;
    private String requestId;
    private String myName;
    private int status;//用于判断是发送申请还是同意申请
    private ArrayList<Post> posts;
    private byte[] avatarData;
    public AddFriendRequest(String friendId, String requestId,String myName, int status, ArrayList<Post> posts,byte[] avatarData) {
        this.friendId = friendId;
        this.requestId = requestId;
        this.myName = myName;
        this.status = status;
        this.posts = posts;
        this.avatarData = avatarData;
    }
    public byte[] getAvatarData() {
        return avatarData;
    }
    public String getFriendId() {
        return friendId;
    }
    public String getRequestId() {
        return requestId;
    }
    public String getName() {
        return myName;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddFriendRequest that = (AddFriendRequest) o;
        return Objects.equals(friendId, that.friendId) && Objects.equals(requestId, that.requestId) && Objects.equals(myName, that.myName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendId, requestId, myName);
    }
     public ArrayList<Post> getPosts() {
        return posts;
    }
    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }
}
