package com.chdz.model;

import java.io.Serializable;
import java.util.*;

//用户类
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String,FriendInfo> friends = new HashMap<>();
    //用户名(昵称)
    private String name;
    //账号
    private String account;
    //密码
    private String password;
    //邮箱
    private String email;

    private byte[] avatar;

    public User(String name, String account, String password, String email) {
        this.name = name;
        this.account = account;
        this.password = password;
        this.email = email;
        friends = new HashMap<>();
    }
    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.email = email;
    }
    public Map<String,FriendInfo> getFriends() {
        return friends;
    }

    public void setFriends(Map<String,FriendInfo> friends) {
        this.friends = friends;
    }
    public void addFriend(FriendInfo friend) {
        friends.put(friend.getId(),friend);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        //账户作为唯一标识码
        return Objects.equals(account, user.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    public String toString() {
        return account + " " + name + " " + email + " " + friends.keySet();
    }


}
