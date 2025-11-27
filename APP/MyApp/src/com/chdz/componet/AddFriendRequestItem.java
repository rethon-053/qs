package com.chdz.componet;

import com.chdz.model.AddFriendRequest;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;

public class AddFriendRequestItem extends JPanel {

    public AddFriendRequestItem(AddFriendRequest req, UserListerPanel parent, ClientSocket clientSocket) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("来自: " + req.getFriendId() + " (" + req.getName() + ")");
        JButton accept = new JButton("同意");
        JButton reject = new JButton("拒绝");

        add(label);
        add(accept);
        add(reject);

        accept.addActionListener(e -> parent.acceptRequest(req));
        reject.addActionListener(e -> parent.rejectRequest(req));
    }
}

