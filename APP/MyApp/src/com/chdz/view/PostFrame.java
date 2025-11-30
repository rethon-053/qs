package com.chdz.view;

import com.chdz.global.AppRunTimeData;
import com.chdz.model.Post;
import com.chdz.network.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

// 发布动态界面
public class PostFrame extends JFrame {
    private ClientSocket clientSocket;
    private JTextArea contentArea;
    private JPanel imagePreviewPanel;
    private List<byte[]> imageDataList;
    private List<ImageIcon> previewIcons;

    public PostFrame() {
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
        this.imageDataList = new ArrayList<>();
        this.previewIcons = new ArrayList<>();
        init();
    }

    private void init() {
        setTitle("发布动态");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建内容输入区域
        contentArea = new JTextArea(6, 40);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createTitledBorder("动态内容"));
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        // 创建图片预览面板
        imagePreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imagePreviewPanel.setBorder(BorderFactory.createTitledBorder("预览图片"));
        imagePreviewPanel.setPreferredSize(new Dimension(580, 200));
        JScrollPane imageScrollPane = new JScrollPane(imagePreviewPanel);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 添加图片按钮
        JButton addImageButton = new JButton("添加图片");
        addImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addImage();
            }
        });

        // 取消按钮
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭窗口
            }
        });

        // 发送按钮
        JButton sendButton = new JButton("发送");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendPost();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(PostFrame.this, "发送动态失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 添加按钮到面板
        buttonPanel.add(addImageButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);

        // 添加组件到主面板
        mainPanel.add(contentScrollPane, BorderLayout.NORTH);
        mainPanel.add(imageScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // 添加图片方法
    private void addImage() {
        // 限制最多添加9张图片
        if (imageDataList.size() >= 9) {
            JOptionPane.showMessageDialog(this, "最多只能添加9张图片", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择图片");
        fileChooser.setMultiSelectionEnabled(true); // 允许选择多张图片

        // 添加图片过滤器
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件 (.jpg, .jpeg, .png, .gif)", "jpg", "jpeg", "png", "gif"));

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            for (File file : selectedFiles) {
                // 检查是否会超过9张图片限制
                if (imageDataList.size() >= 9) {
                    JOptionPane.showMessageDialog(this, "已达到最大图片数量限制", "提示", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }

                try {
                    // 读取图片数据
                    FileInputStream fis = new FileInputStream(file);
                    byte[] imageData = new byte[(int) file.length()];
                    fis.read(imageData);
                    fis.close();

                    // 检查图片大小（限制5MB）
                    if (imageData.length > 5 * 1024 * 1024) {
                        JOptionPane.showMessageDialog(this, "图片过大，请选择小于5MB的图片", "错误", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    // 创建预览图标
                    ImageIcon originalIcon = new ImageIcon(imageData);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    ImageIcon previewIcon = new ImageIcon(scaledImage);

                    // 添加到列表
                    imageDataList.add(imageData);
                    previewIcons.add(previewIcon);

                    // 更新预览面板
                    updateImagePreviewPanel();

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "读取图片失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // 更新图片预览面板
    private void updateImagePreviewPanel() {
        imagePreviewPanel.removeAll();

        for (int i = 0; i < previewIcons.size(); i++) {
            final int index = i;
            JPanel imageContainer = new JPanel(new BorderLayout());

            // 添加预览图片
            JLabel imageLabel = new JLabel(previewIcons.get(i));
            imageContainer.add(imageLabel, BorderLayout.CENTER);

            // 添加删除按钮
            JButton removeButton = new JButton("×");
            removeButton.setForeground(Color.RED);
            removeButton.setPreferredSize(new Dimension(20, 20));
            removeButton.setFocusPainted(false);
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeImage(index);
                }
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(removeButton);
            imageContainer.add(buttonPanel, BorderLayout.NORTH);

            imageContainer.setPreferredSize(new Dimension(120, 120));
            imagePreviewPanel.add(imageContainer);
        }

        imagePreviewPanel.revalidate();
        imagePreviewPanel.repaint();
    }

    // 移除图片
    private void removeImage(int index) {
        if (index >= 0 && index < imageDataList.size()) {
            imageDataList.remove(index);
            previewIcons.remove(index);
            updateImagePreviewPanel();
        }
    }

    // 发送动态
    private void sendPost() throws IOException {
        String content = contentArea.getText().trim();

        // 检查内容不为空
        if (content.isEmpty() && imageDataList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "动态内容不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 创建Post对象
        String userId = AppRunTimeData.getInstance().getCurrentUser().getAccount();
        String username = AppRunTimeData.getInstance().getCurrentUser().getName();
        Post post = new Post(content, username, userId);

        // 添加图片
        for (byte[] imageData : imageDataList) {
            post.addIcon(imageData);
        }


        // 发送到服务器
        clientSocket.sendPost(post);


        // 显示成功消息并关闭窗口
        JOptionPane.showMessageDialog(this, "动态发布成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}