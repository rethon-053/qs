package com.chdz.componet;

import com.chdz.global.AppRunTimeData;
import com.chdz.network.ClientSocket;
import com.chdz.view.ChatMainFrame;

import javax.swing.*;
import java.awt.*;

// 动态面板 - 显示朋友圈动态
public class TrackPanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private JTextArea trackArea;

    public TrackPanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        // 创建一个文本区域显示动态内容
        trackArea = new JTextArea();
        trackArea.setEditable(false);
        trackArea.setLineWrap(true);
        trackArea.setWrapStyleWord(true);

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(trackArea);
        add(scrollPane, BorderLayout.CENTER);


    }

//    // 显示模拟动态内容
//    private void showMockTracks() {
//        StringBuilder tracks = new StringBuilder();
//        tracks.append("=== 朋友圈动态 ===\n\n");
//        tracks.append("【张三】\n今天天气真好！\n2023-05-15 10:30\n\n");
//        tracks.append("【李四】\n分享一首好听的歌曲《晴天》- 周杰伦\n2023-05-14 18:45\n\n");
//        tracks.append("【王五】\n刚完成一个大项目，感觉轻松多了！\n2023-05-14 15:20\n\n");
//        tracks.append("【赵六】\n周末去爬山，风景真不错！\n2023-05-13 20:10\n\n");
//        tracks.append("【钱七】\n推荐一部好电影《流浪地球2》\n2023-05-12 22:30\n\n");
//
//        trackArea.setText(tracks.toString());
//    }
}
