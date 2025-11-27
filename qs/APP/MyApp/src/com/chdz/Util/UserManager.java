package com.chdz.Util;

import com.chdz.model.User;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    public static Map<String, User> userMap = new HashMap<String, User>();
    public static void addUser(User user) {
        userMap.put(user.getAccount(), user);
    }
    public static User getUser(String account) {
        return userMap.get(account);
    }
    public static boolean checkUser(String account, String password) {
        User user = getUser(account);
        return user != null && user.getPassword().equals(password);
    }
    public synchronized static void saveData() {
        // 确保data目录存在（项目根目录下的data文件夹）
        File dataDir = new File("App/MyApp/src/com/chdz/data/");
        if (!dataDir.exists()) {
            dataDir.mkdirs(); // 递归创建目录
        }
        // 数据文件路径：data/user.ser
        File file = new File(dataDir, "user.ser");
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
            oos.writeObject(userMap);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadData() {
        // 加载路径与保存路径一致：data/user.ser
        File file = new File("App/MyApp/src/com/chdz/data/user.ser");
        if (!file.exists()) {
            return;
        }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
            userMap = (Map<String, User>) ois.readObject();
        } catch (java.io.IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
