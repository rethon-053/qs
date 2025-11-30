package com.chdz.componet;

import com.chdz.Util.AvatarUtil;
import com.chdz.model.FriendInfo;
import com.chdz.view.PostFrame;
import com.chdz.global.AppRunTimeData;
import com.chdz.model.Post;
import com.chdz.network.ClientSocket;
import com.chdz.view.ChatMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

// 动态面板 - 显示朋友圈动态
public class TrackPanel extends JPanel {
    private ChatMainFrame parent;
    private ClientSocket clientSocket;
    private JButton postButton;
    private JPanel postsDisplayPanel;
    private JScrollPane scrollPane;

    private ArrayList<Post> postList = new ArrayList<>();

    public TrackPanel(ChatMainFrame parent) {
        this.parent = parent;
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        // 创建发布按钮
        postButton = new JButton("发布动态");
        postButton.addActionListener(e -> postButtonClicked());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(postButton);

        postsDisplayPanel = new JPanel();
        postsDisplayPanel.setLayout(new BoxLayout(postsDisplayPanel, BoxLayout.Y_AXIS));
        postsDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(postsDisplayPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        updatePostList(clientSocket.getRcvPosts());
    }

    public void postButtonClicked() {
        PostFrame postFrame = new PostFrame();
        postFrame.setVisible(true);
    }

    public void loadPosts() {
        postsDisplayPanel.removeAll();

        postList.sort((p1, p2) -> Long.compare(p2.getTime(), p1.getTime()));
        for (Post post : postList) {
            addPostPanel(post);
        }
        if (postList.isEmpty()) {
            showEmptyState();
        }

        postsDisplayPanel.revalidate();
        postsDisplayPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMinimum());
        });
    }

    public void addPostPanel(Post post) {
        JPanel postPanel = new JPanel();
        postPanel.setLayout(new BorderLayout());
        postPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));


        JPanel headerPanel = getAvatarPanel(post);

        JPanel userInfoPanel = getuserInfoPanel(post);

        headerPanel.add(userInfoPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        JTextArea contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
        contentPanel.add(contentArea, BorderLayout.NORTH);

        if (!post.getIcons().isEmpty()) {
            JPanel iconPanel = createImagePanel(post.getIcons());
            contentPanel.add(iconPanel, BorderLayout.CENTER);
        }

        postPanel.add(headerPanel, BorderLayout.NORTH);
        postPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel spacingPanel = new JPanel(new BorderLayout());
        spacingPanel.setPreferredSize(new Dimension(0, 8));

        postsDisplayPanel.add(postPanel);
        postsDisplayPanel.add(spacingPanel);
    }

    private static JPanel getuserInfoPanel(Post post) {
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));

        JLabel userNameLabel = new JLabel(post.getUserName());
        JLabel timeLabel = new JLabel(post.getTimeString());

        userNameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        timeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        timeLabel.setForeground(Color.GRAY);

        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(timeLabel);
        return userInfoPanel;
    }

    private static JPanel getAvatarPanel(Post post) {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        byte[] avatarData = null;
        String senderId = post.getSenderId();
        System.out.println(senderId);
        System.out.println(AppRunTimeData.getInstance().getCurrentUser().getFriends().keySet());
        if (senderId.equals(AppRunTimeData.getInstance().getCurrentUser().getAccount())) {
            avatarData = AppRunTimeData.getInstance().getCurrentUser().getAvatar();
        } else {
            avatarData = AppRunTimeData.getInstance().getCurrentUser().getFriends().get(senderId).getAvatar();
        }
        JLabel avatarLabel = AvatarUtil.createAvatarLabel(
                avatarData,
                AvatarUtil.DEFAULT_AVATAR_SIZE
        );
        headerPanel.add(avatarLabel);


        return headerPanel;
    }

    private JPanel createImagePanel(ArrayList<byte[]> imageDataList) {
        JPanel imagePanel = new JPanel();

        // 根据图片数量决定布局
        if (imageDataList.isEmpty()) {
            return imagePanel;
        }
        if (imageDataList.size() == 1) {
            // 单张图片，居中显示
            imagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            addSingleImage(imagePanel, imageDataList.get(0));
        } else if (imageDataList.size() == 2 || imageDataList.size() == 4) {
            // 2张或4张图片，2x2网格
            imagePanel.setLayout(new GridLayout(2, 2, 5, 5));
            for (byte[] imageData : imageDataList) {
                addGridImage(imagePanel, imageData);
            }
        } else {
            // 其他数量，使用流式布局
            imagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            for (byte[] imageData : imageDataList) {
                addFlowImage(imagePanel, imageData);
            }
        }

        return imagePanel;
    }

    private void addSingleImage(JPanel imagePanel, byte[] imageData) {
        ImageIcon icon = new ImageIcon(imageData);

        int maxWidth = 400;
        int maxHeight = 300;
        Image scaledImage = scaleImage(icon.getImage(), maxWidth, maxHeight);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);

        addImageClickListener(imageLabel,scaledIcon);
        imagePanel.add(imageLabel);
    }
    private void addGridImage(JPanel imagePanel, byte[] imageData) {
        ImageIcon icon = new ImageIcon(imageData);

        int targetSize = 180;
        Image scaledImage = scaleImage(icon.getImage(), targetSize, targetSize);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        addImageClickListener(imageLabel,scaledIcon);
        imagePanel.add(imageLabel);
    }
    private void addFlowImage(JPanel imagePanel, byte[] imageData) {
        ImageIcon icon = new ImageIcon(imageData);

        int targetSize = 150;
        Image scaledImage = scaleImage(icon.getImage(), targetSize, targetSize);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel imageLabel = new JLabel(scaledIcon);

        addImageClickListener(imageLabel,scaledIcon);
        imagePanel.add(imageLabel);
    }
    public Image scaleImage(Image image, int maxWidth, int maxHeight) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        if (width <= maxWidth && height <= maxHeight) {
            return image;
        }

        double widthRatio = (double) maxWidth / width;
        double heightRatio = (double) maxHeight / height;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }
    private void addImageClickListener(JLabel imageLabel, ImageIcon icon) {
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 处理图片点击事件
               JDialog imageDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(TrackPanel.this),"图片预览",true);
               imageDialog.setSize(800,600);
               imageDialog.setLocationRelativeTo(null);

               JScrollPane scrollPane1 = new JScrollPane(new JLabel(icon));
               imageDialog.add(scrollPane1);

               scrollPane1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // 处理图片点击事件
                        imageDialog.dispose();
                    }
                });

               imageDialog.setVisible(true);
            }
        });
    }

    public void showEmptyState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(500, 200));
        emptyPanel.setLayout(new BorderLayout());

        JLabel emptyLabel = new JLabel("暂无动态", SwingConstants.CENTER);
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setVerticalAlignment(SwingConstants.CENTER);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);

        postsDisplayPanel.add(emptyPanel);
    }

    public void updatePostList(ArrayList<Post> newPosts){
        this.postList = newPosts;
        loadPosts();
    }
}
