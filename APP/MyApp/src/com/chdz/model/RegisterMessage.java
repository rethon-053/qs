package com.chdz.model;

// 注册消息
public class RegisterMessage extends Message {
    private String account;
    private String password;
    private String nickname;
    private String email;

    public RegisterMessage(String nickname, String account, String password, String email) {
        super("client");
        this.nickname = nickname;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
