package com.chdz.model;

import java.io.Serializable;
import java.util.Objects;

public class AddFriendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String friendId;
    private String requestId;
    private String myName;
    private int status;//用于判断是发送申请还是同意申请
    public AddFriendRequest(String friendId, String requestId,String myName, int status) {
        this.friendId = friendId;
        this.requestId = requestId;
        this.myName = myName;
        this.status = status;
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
}
