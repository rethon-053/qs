package com.chdz.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class AvatarUtil {
    // 默认头像尺寸
    public static final int DEFAULT_AVATAR_SIZE = 30;
    public static final int USER_AVATAR_SIZE = 50;
    public static final int CHAT_AVATAR_SIZE = 40;
    public static final int SHOW_AVATAR_SIZE = 80;


    // 将byte数组转换为ImageIcon
    public static ImageIcon getAvatarFromBytes(byte[] avatarData, int size) {
        if (avatarData == null || avatarData.length == 0) {
            return getDefaultAvatar(size);
        }
        
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(avatarData));
            Image scaledImage = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            e.printStackTrace();
            return getDefaultAvatar(size);
        }
    }
    
    // 获取默认头像
    public static ImageIcon getDefaultAvatar(int size) {
        try {
            BufferedImage defaultAvatar = ImageIO.read(AvatarUtil.class.getResourceAsStream("/images/default.png"));
            Image scaledImage = defaultAvatar.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultAvatar(size);
        }
    }

    
    // 创建圆形头像标签
    public static JLabel createAvatarLabel(byte[] avatarData, int size) {
        ImageIcon icon = getAvatarFromBytes(avatarData, size);
        JLabel label = new JLabel(icon);
        label.setPreferredSize(new Dimension(size, size));
        label.setBorder(BorderFactory.createEmptyBorder());
        return label;
    }
}