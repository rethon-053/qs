package com.chdz.network;
import com.chdz.Util.UserManager;
import com.chdz.model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.chdz.Util.UserManager.userMap;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket sk = new ServerSocket(8888);

        UserManager.loadData();
        Set<Map.Entry<String, User>> set = userMap.entrySet();
        for (Map.Entry<String, User> stringUserEntry : set) {
            System.out.println(sk.getLocalPort() + ": " + stringUserEntry.getKey());
        }
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 10, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        ArrayList<Socket> list = new ArrayList<>();
        HashMap<String,Socket> map = new HashMap<>();
        while (true) {
            final Socket s = sk.accept();
            list.add(s);
            System.out.println("accept: " + s.getInetAddress().getHostAddress());
            tpe.submit(new Thread(() -> {
                new Thread(() -> {
                    String account = "";
                    try {
                        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

                        while (true){
                            Object msg = ois.readObject();
                            if (msg instanceof LogoutMessage logoutMsg) {
                                if (map.containsKey(logoutMsg.getUserId())) {
                                    map.remove(logoutMsg.getUserId());
                                }
                            }
                            if (msg instanceof LoginMessage loginMsg) {
                                String ac = loginMsg.getAccount();
                                String pw = loginMsg.getPassword();
                                if (map.containsKey(ac)) {
                                    oos.writeObject(new TextMessage("Account already login"));
                                    oos.flush();
                                    continue;
                                }
                                if(userMap.containsKey(ac) && pw.equals(userMap.get(ac).getPassword())){
                                    oos.writeObject(new TextMessage("Login Success"));
                                    Map<String, ArrayList<ChatMessage>> msgs = new HashMap<>();
                                    File file = new File("App/MyApp/src/com/chdz/data/Message");
                                    for (File f : file.listFiles()) {
                                        String[] ids = f.getName().split("_");
                                        ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(f));
                                        if (ids[0].equals(ac) || ids[1].equals(ac)) {
                                            String friend = ids[0].equals(ac) ? ids[1] : ids[0];
                                            msgs.put(friend, (ArrayList<ChatMessage>) ois2.readObject());
                                        }
                                        ois2.close();
                                    }
                                    map.put(ac,s);
                                    oos.writeObject(msgs);
                                    oos.writeObject(userMap.get(ac).getFriends());
                                    account = ac;
                                } else {

                                    oos.writeObject(new TextMessage("Login Failed"));
                                }
                                oos.flush();
                            }
                            if (msg instanceof RegisterMessage r) {
                                String ac = r.getAccount();
                                if (map.containsKey(ac)) {
                                    oos.writeObject(new TextMessage("Account Already Exists"));
                                } else {
                                    String name = r.getNickname();
                                    String pw = r.getPassword();
                                    String em = r.getEmail();
                                    User user = new User(name, ac, pw, em);
                                    UserManager.addUser(user);
                                    UserManager.saveData();
                                    oos.writeObject(new TextMessage("Register Success"));
                                    oos.flush();
                                }
                            }
                            if (msg instanceof ChatMessage cm) {
                                String to = cm.getTo();
                                if (map.containsKey(to)) {
                                    ObjectOutputStream oosTo = new ObjectOutputStream(map.get(to).getOutputStream());
                                    oosTo.writeObject(cm);
                                    oosTo.flush();
                                }
                                String from = cm.getFrom();
                                String[] ids = new String[]{from,to};
                                Arrays.sort(ids);
                                String desk = ids[0] + "_" + ids[1];
                                File file = new File("App/MyApp/src/com/chdz/data/Message/" + desk);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                ObjectInputStream on = new ObjectInputStream(new FileInputStream(file));
                                ArrayList<ChatMessage> listMsg = (ArrayList<ChatMessage>) on.readObject();
                                listMsg.add(cm);
                                on.close();
                                ObjectOutputStream oosTo = new ObjectOutputStream(new FileOutputStream(file));
                                oosTo.writeObject(listMsg);
                                oosTo.flush();
                            }
                        }
                    } catch (IOException e) {
                        map.remove(account);
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }));
        }
    }
}


