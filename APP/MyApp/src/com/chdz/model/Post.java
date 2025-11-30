package com.chdz.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Post extends Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String content;
    private long time;
    private String userName;
    private ArrayList<byte[]> iconDataList;

    public Post(String content, String userName, String userId) {
        super(userId);
        this.content = content;
        this.time = System.currentTimeMillis();
        this.userName = userName;
        this.iconDataList = new ArrayList<>();
    }
    public String getContent() {
        return content;
    }
    public long getTime() {
        return time;
    }
    public String getUserName() {
        return userName;
    }
    public ArrayList<byte[]> getIcons() {
       return iconDataList;
    }
    public void addIcon(byte[] iconData) {
        iconDataList.add(iconData);
    }
    public String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }
}
