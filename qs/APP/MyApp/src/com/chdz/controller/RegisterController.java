package com.chdz.controller;
import com.chdz.Util.IOMethod;
import com.chdz.model.User;

import java.util.HashSet;

//注册控制器
public class RegisterController
{
    //控制写入
    public static boolean WriteAllUsers(User NewUser)
    {
        //读出用户文件集合
        HashSet<User> users = IOMethod.getAllUser();
        //查重 如果成功
       if(users.contains(NewUser))
       {
           return false;
       }
        else
       {
           //写入新的用户进入集合
           users.add(NewUser);
           IOMethod.inputAllUsers(users);
           return true;
       }
    }
}

