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
        HashMap<String,Socket> connectedUsers = new HashMap<>();
        HashMap<String, ObjectOutputStream> oosMap = new HashMap<>();
        HashMap<String, READ_STATUS_UPDATE> readStatusUpdateMap = getStringREADStatusUpdateHashMap();


        while (true) {
            final Socket s = sk.accept();
            list.add(s);
            for (Map.Entry<String, Socket> entry : connectedUsers.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getInetAddress().getHostAddress());
            }
            System.out.println("accept: " + s.getInetAddress().getHostAddress());
            tpe.submit(new Thread(() -> {
                new Thread(() -> {
                    String account = "";
                    try {
                        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

                        while (true){
                            Object msg = ois.readObject();
                            if (msg instanceof READ_STATUS_UPDATE read) {
                                readStatusUpdateMap.put(read.getSenderId()+"_"+read.getReceiverId(), read);
                                File file = new File("App/MyApp/src/com/chdz/data/ReadStatus");
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                try {
                                    System.out.println("write readStatusUpdateMap to file");
                                    System.out.println(read.getSenderId() + " " + read.getReceiverId() + " " + read.getSendTime());
                                    ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(file));
                                    oos2.writeObject(readStatusUpdateMap);
                                    oos2.flush();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (msg instanceof User user) {
                                account = user.getAccount();
                                userMap.put(account,user);
                                UserManager.saveData();
                                continue;
                            }
                            if (msg instanceof AddFriendRequest add) {
                                String friendId = add.getFriendId();
                                if (add.getStatus() == 3) {
                                    String del = account + "_" + friendId;
                                    File file = new File("App/MyApp/src/com/chdz/data/AddRequest/" + del);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    continue;
                                }
                                if (account.equals(friendId)) {
                                    oos.writeObject("Cannot add self");
                                    continue;
                                }
                                if (userMap.containsKey(friendId)) {
                                    if (add.getStatus() == 0) {
                                        oos.writeObject("Add Request Sent");
                                    }
                                    if (add.getStatus() == 1 || add.getStatus() == 2) {
                                        String del = friendId + "_" + account;
                                        File file = new File("App/MyApp/src/com/chdz/data/AddRequest/" + del);
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    }

                                    if (connectedUsers.containsKey(friendId)) {
                                        ObjectOutputStream oos2 = oosMap.get(friendId);
                                        oos2.writeObject(add);
                                        oos2.flush();
                                    }
                                    String desk = account + "_" + friendId;
                                    File file = new File("App/MyApp/src/com/chdz/data/AddRequest/" + desk);
                                    if (!file.exists()) {
                                        file.createNewFile();
                                    }
                                    try (FileOutputStream fos = new FileOutputStream(file)) {
                                        ObjectOutputStream oos2 = new ObjectOutputStream(fos);
                                        oos2.writeObject(add);
                                        oos2.flush();
                                        oos2.close();
                                    }

                                } else {
                                    oos.writeObject("Add Request Failed");
                                }
                                oos.flush();
                            }
                            if (msg instanceof LogoutMessage logoutMsg) {
                               connectedUsers.remove(logoutMsg.getUserId());
                            }
                            if (msg instanceof LoginMessage loginMsg) {
                                String ac = loginMsg.getAccount();
                                String pw = loginMsg.getPassword();
                                if (connectedUsers.containsKey(ac)) {
                                    oos.writeObject("Account already login");
                                    oos.flush();
                                    continue;
                                }
                                if(userMap.containsKey(ac) && pw.equals(userMap.get(ac).getPassword())){
                                    account = ac;
                                    oos.writeObject(userMap.get(ac));
                                    oos.flush();
                                    Map<String, ArrayList<ChatMessage>> msgs = getStringArrayListMap(ac);
                                    Map<String, Map<String, ArrayList<ChatMessage>>> chatMessagesMap = new HashMap<>();
                                    chatMessagesMap.put("chat", msgs);
                                    ArrayList<AddFriendRequest> adds = new ArrayList<>();
                                    File file2 = new File("App/MyApp/src/com/chdz/data/AddRequest");
                                    for (File f : Objects.requireNonNull(file2.listFiles())) {
                                        String[] ids = f.getName().split("_");
                                        if (ids[1].equals(ac)) {
                                            ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(f));
                                            adds.add((AddFriendRequest) ois2.readObject());
                                            ois2.close();
                                        }
                                    }
                                    Map<String, READ_STATUS_UPDATE> reads = new HashMap<>();
                                    for (Map.Entry<String, READ_STATUS_UPDATE> entry : readStatusUpdateMap.entrySet()) {
                                        String[] ids = entry.getKey().split("_");
                                        if (ids[1].equals(ac)) {
                                            reads.put(ids[0], entry.getValue());
                                        }
                                        System.out.println(ids[0] + " " + ids[1] + " " + entry.getValue().getSendTime());
                                    }
                                    Map<String, Map<String, READ_STATUS_UPDATE>> readMap = new HashMap<>();
                                    readMap.put("read", reads);
                                    oos.writeObject(readMap);
                                    oos.writeObject(adds);
                                    oosMap.put(ac,oos);
                                    connectedUsers.put(ac,s);
                                    oos.writeObject(chatMessagesMap);
                                    oos.writeObject("Login Success");
                                    oos.flush();
                                } else {

                                    oos.writeObject("Login Failed");
                                }
                                oos.flush();
                            }

                            if (msg instanceof RegisterMessage r) {
                                String ac = r.getAccount();
                                if (connectedUsers.containsKey(ac)) {
                                    oos.writeObject("Account Already Exists");
                                } else {
                                    String name = r.getNickname();
                                    String pw = r.getPassword();
                                    String em = r.getEmail();
                                    User user = new User(name, ac, pw, em);
                                    UserManager.addUser(user);
                                    UserManager.saveData();
                                    oos.writeObject("Register Success");
                                    oos.flush();
                                }
                            }
                            if (msg instanceof ChatMessage cm) {
                                String to = cm.getTo();
                                if (connectedUsers.containsKey(to)) {
                                    ObjectOutputStream oosTo = oosMap.get(to);
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
                                ArrayList<ChatMessage> listMsg;
                                try {
                                    ObjectInputStream on = new ObjectInputStream(new FileInputStream(file + "/messages.dat"));
                                    listMsg = (ArrayList<ChatMessage>) on.readObject();
                                    on.close();
                                }
                                catch (IOException e) {
                                    listMsg = new ArrayList<>();
                                }
                                listMsg.add(cm);
                                ObjectOutputStream oosTo = new ObjectOutputStream(new FileOutputStream(file + "/messages.dat"));
                                oosTo.writeObject(listMsg);
                                oosTo.flush();
                                oosTo.close();
                            }
                        }
                    } catch (IOException e) {
                        connectedUsers.remove(account);
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }));
        }
    }

    private static HashMap<String, READ_STATUS_UPDATE> getStringREADStatusUpdateHashMap() throws IOException {
        HashMap<String, READ_STATUS_UPDATE> readStatusUpdateMap = null;
        File file = new File("App/MyApp/src/com/chdz/data/ReadStatus");
        if(file.exists()){
            ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(file));
            try {
                readStatusUpdateMap = (HashMap<String, READ_STATUS_UPDATE>) ois2.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            ois2.close();
        }
        if (readStatusUpdateMap == null) {
            readStatusUpdateMap = new HashMap<>();
        }
        return readStatusUpdateMap;
    }

    private static Map<String, ArrayList<ChatMessage>> getStringArrayListMap(String ac) throws IOException, ClassNotFoundException {
        Map<String, ArrayList<ChatMessage>> msgs = new HashMap<>();
        File file = new File("App/MyApp/src/com/chdz/data/Message");
        for (File f : Objects.requireNonNull(file.listFiles())) {
            String[] ids = f.getName().split("_");
            File msgFile = new File(f + "/messages.dat");
            if (!msgFile.exists()) {
                msgFile.createNewFile();
            }
            if (ids[0].equals(ac) || ids[1].equals(ac)) {
                String friend = ids[0].equals(ac) ? ids[1] : ids[0];
                ArrayList<ChatMessage> m;
                try {
                    ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(msgFile));
                    m = (ArrayList<ChatMessage>) ois2.readObject();
                } catch (IOException e) {
                    m = new ArrayList<>();
                }
                msgs.put(friend, m);

            }
        }
        return msgs;
    }
}


