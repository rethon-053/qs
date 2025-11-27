package com.chdz.Util;

import com.chdz.model.User;

import java.io.*;
import java.util.HashSet;

public class IOMethod
{
    //读入用户集合
    static File file = new File("APP//MyApp//src//resources//Users.obj");
    public static synchronized HashSet<User> getAllUser()
    {
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(file.exists())
        {
            try(FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis))
            {
                return (HashSet<User>)ois.readObject();
            }
            catch (ClassNotFoundException | IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        //找不到文件就返回一个空集合
        else return new HashSet<User>();
    }

    //保存用户集合
    public static synchronized void inputAllUsers(HashSet<User> users)
    {
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try (FileOutputStream os = new FileOutputStream(file);
             ObjectOutputStream ois = new ObjectOutputStream(os))
        {
            ois.writeObject(users);
            ois.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
