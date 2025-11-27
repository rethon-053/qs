package com.chdz.model;

public class TextMessage extends Message {
    private String text;
    public TextMessage(String text) {
        super(MessageType.SYS,"System");
        this.text = text;
    }
    public String getText() {
        return text;
    }
}
