package com.chdz.model;

import java.util.Objects;

public class Message {
    //消息发送者
    private String from;
    //消息接收者
    private String receive;
    //发送时间
    private String time;
    //发送内容
    private String message;
    //消息类型(私聊 群聊 系统提醒)
    private String type;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReceive() {
        return receive;
    }

    public void setReceive(String receive) {
        this.receive = receive;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(from, message1.from) && Objects.equals(receive, message1.receive) && Objects.equals(time, message1.time) && Objects.equals(message, message1.message) && Objects.equals(type, message1.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, receive, time, message, type);
    }
}
