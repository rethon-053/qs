package com.chdz.model;

// 登录消息
public class LoginMessage extends Message {
    private String account;
    private String password;
    
    public LoginMessage(String account, String password) {
        super("client");
        this.account = account;
        this.password = password;
    }
    
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
