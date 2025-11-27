package com.chdz.view;

import com.chdz.global.AppRunTimeData;
import com.chdz.global.Const;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//注册界面

import com.chdz.network.ClientSocket;

import java.io.IOException;

//注册界面
public class RegisterFrame extends AbstractAppView {
    // 组件声明
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField accountField;
    private JTextField emailField;
    private JTextField nameFiled;
    private JButton registerButton;
    private JButton backButton;
    private JLabel titleLabel;
    private ClientSocket socket;

    // 背景颜色
    private Color backgroundColor = new Color(240, 248, 255);
    private Color primaryColor = new Color(0, 120, 215);
    private Color lightGray = new Color(245, 245, 245);

    public RegisterFrame() {
        this.socket = AppRunTimeData.getInstance().getClientSocket();
    }
    @Override
    protected void init() {
        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        frame.setTitle("用户注册");
        setLayout(null);
        setBackground(backgroundColor);

        // 初始化UI组件
        initComponents();

        // 设置组件事件
        setupEvents();
        //如果涉及到页面跳转
        //  调用这个方法即可 changeView("要跳转的页面");
    }

    private void initComponents() {
        // 标题
        titleLabel = new JLabel("用户注册", JLabel.CENTER);
        titleLabel.setBounds(0, 60, Const.WIDTH, 50);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(primaryColor);
        add(titleLabel);

        // 密码区域
        JLabel passwordLabel = createLabel("密码:", 310);
        add(passwordLabel);

        passwordField = createPasswordField(310);
        add(passwordField);

        // 确认密码区域
        JLabel confirmPasswordLabel = createLabel("确认密码:", 370);
        add(confirmPasswordLabel);

        confirmPasswordField = createPasswordField(370);
        add(confirmPasswordField);

        // 邮箱区域
        JLabel emailLabel = createLabel("邮箱:", 250);
        add(emailLabel);

        emailField = createTextField(250);
        add(emailField);

        // 用户名区域
        JLabel nameLabel = createLabel("用户名:", 130);
        add(nameLabel);

        nameFiled = createTextField(130);
        add(nameFiled);

        //账号区域
        JLabel accountLabel = createLabel("账号",190);
        add(accountLabel);

        accountField = createTextField(190);
        add(accountField);



        // 注册按钮
        registerButton = createPrimaryButton("注册", 430);
        add(registerButton);

        // 返回按钮
        backButton = createSecondaryButton("返回登录", 480);
        add(backButton);
    }

    private JLabel createLabel(String text, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(50, y, 80, 30);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private JTextField createTextField(int y) {
        JTextField field = new JTextField();
        field.setBounds(130, y, 200, 35);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
        return field;
    }

    private JPasswordField createPasswordField(int y) {
        JPasswordField field = new JPasswordField();
        field.setBounds(130, y, 200, 35);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
        return field;
    }

    private JButton createPrimaryButton(String text, int y) {
        JButton button = new JButton(text);
        button.setBounds(100, y, 200, 40);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(primaryColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });

        return button;
    }

    private JButton createSecondaryButton(String text, int y) {
        JButton button = new JButton(text);
        button.setBounds(100, y, 200, 35);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setBackground(lightGray);
        button.setForeground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(220, 220, 220));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(lightGray);
            }
        });

        return button;
    }

    private void setupEvents() {
        // 注册按钮事件
        registerButton.addActionListener(e -> {
            try {
                performRegistration();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });


        // 返回按钮事件
        backButton.addActionListener(e -> {
            try {
                Class<?> loginClass = Class.forName("com.chdz.view.LoginFrame");
                AbstractAppView loginView = (AbstractAppView) loginClass.newInstance();
                changeView(loginView);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "请先创建LoginFrame类", "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // 回车键注册功能
        passwordField.addActionListener(e -> {
            try {
                performRegistration();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        confirmPasswordField.addActionListener(e -> {
            try {
                performRegistration();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        emailField.addActionListener(e -> {
            try {
                performRegistration();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void performRegistration() throws IOException {
        String password = new String(passwordField.getPassword());
        String account = new String (accountField.getText().trim());
        String name = new String(nameFiled.getText().trim());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();

        // 输入验证
        if (!validateInput(password, confirmPassword, email)) {
            return;
        }

        // 执行注册逻辑
        socket.sendRegister(name,account, password, email);


    }

    private boolean validateInput(String password, String confirmPassword, String email) {
        if (password.isEmpty()) {
            showError("密码不能为空");
            passwordField.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showError("密码长度不能少于6个字符");
            passwordField.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            confirmPasswordField.requestFocus();
            return false;
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            showError("请输入有效的邮箱地址");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "输入错误",
                JOptionPane.WARNING_MESSAGE);
    }



    @Override
    protected void draw(long dlt) {
        // 重绘界面
        repaint();
    }

    // 自定义绘制背景
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制渐变背景
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(245, 250, 255),
                getWidth(), getHeight(), new Color(235, 245, 255)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}