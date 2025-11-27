package com.chdz.view;

import com.chdz.controller.LoginController;
import com.chdz.global.AppRunTimeData;
import com.chdz.global.Const;
import com.chdz.network.ClientSocket;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//登录界面
public class LoginFrame extends AbstractAppView {
    // 组件声明
    private JTextField accountField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton loginButton;
    private String loginBtnPath = "APP/res/images/登录按钮.png";
    private String registerBtnPath = "APP/res/images/注册按钮.png";

    private String account;
    private String password;
    private ClientSocket clientSocket;

    // 按钮位置和尺寸常量
    private final int BUTTON_WIDTH = 80;
    private final int BUTTON_HEIGHT = 30;
    private final int LOGIN_BUTTON_X = 100;
    private final int REGISTER_BUTTON_X = 220;
    private final int BUTTONS_Y = 290; // 240 + 50

    // 构造方法
    public LoginFrame() {
        // 默认构造方法
        this.clientSocket = AppRunTimeData.getInstance().getClientSocket();
    }


    @Override
    protected void init() {
        // 先设置布局和初始化UI
        setLayout(null);
        createUI();

        // 设置窗口容器面板为当前页面
        frame.setContentPane(this);
        // 自动调整窗口大小以适合内容
        frame.pack();
        // 设置标题
        frame.setTitle("开始界面");
    }

    private void createUI() {
        int textWidth = 200;
        int textHeight = 30;
        int baseY = 150;

        // 创建账号文本框
        accountField = new JTextField();
        accountField.setBounds(100, baseY + 50, textWidth, textHeight);
        accountField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        accountField.setForeground(Color.gray);
        accountField.setText("请输入账号");
        setupAccountField();
        add(accountField);

        // 创建密码文本框
        passwordField = new JPasswordField();
        passwordField.setBounds(100, baseY + 100, textWidth, textHeight);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setEchoChar((char)0);
        passwordField.setText("请输入密码");
        passwordField.setForeground(Color.gray);
        setupPasswordField();
        add(passwordField);

        // 创建登录按钮 - 使用常量确保位置一致
        loginButton = new JButton("登录");
        loginButton.setBounds(LOGIN_BUTTON_X, BUTTONS_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        setupLoginButton();
        add(loginButton);

        // 创建注册按钮 - 使用常量确保位置一致
        registerButton = new JButton("注册");
        registerButton.setBounds(REGISTER_BUTTON_X, BUTTONS_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        setupRegisterButton();
        add(registerButton);
    }

    private void setupAccountField() {
        accountField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(accountField.getText().equals("请输入账号")) {
                    accountField.setText("");
                    accountField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                account = accountField.getText().trim();
                if (account.isEmpty()) {
                    accountField.setText("请输入账号");
                    accountField.setForeground(Color.gray);
                }
            }
        });

        accountField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
    }

    private void setupPasswordField() {
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(new String(passwordField.getPassword()).equals("请输入密码")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('*');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                password = new String(passwordField.getPassword()).trim();
                if (password.isEmpty()) {
                    passwordField.setText("请输入密码");
                    passwordField.setForeground(Color.gray);
                    passwordField.setEchoChar((char)0);
                }
            }
        });

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
    }

    private void setupLoginButton() {
        // 设置按钮透明
        hideButton(loginButton);

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                performLogin();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                loginBtnPath = "APP/res/images/登录按下.png";
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginBtnPath = "APP/res/images/登录按钮.png";
                repaint();
            }
        });
    }

    private void setupRegisterButton() {
        // 设置按钮透明
        hideButton(registerButton);

        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 创建RegisterFrame并传递clientSocket
                RegisterFrame registerFrame = new RegisterFrame();
                changeView(registerFrame);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                registerBtnPath = "APP/res/images/注册按下.png";
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerBtnPath = "APP/res/images/注册按钮.png";
                repaint();
            }
        });

        // 添加按钮点击效果
        registerButton.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame();
            changeView(registerFrame);
        });
    }

    // 提取登录逻辑到单独方法
    private void performLogin() {
        account = accountField.getText().trim();
        password = new String(passwordField.getPassword()).trim();

        // 输入验证
        if (account.isEmpty() || account.equals("请输入账号")) {
            showMessage("请输入账号", "提示", JOptionPane.WARNING_MESSAGE);
            accountField.requestFocus();
            return;
        }

        if (password.isEmpty() || password.equals("请输入密码")) {
            showMessage("请输入密码", "提示", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        // 执行登录逻辑
        try {
            new LoginController(this,clientSocket).loginButtonClick();
        } catch (IOException ex) {
            showMessage("登录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    @Override
    protected void draw(long dlt) {
        // 可以在这里添加动画效果或定期更新逻辑
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制背景
        drawBackground(g);
        // 绘制按钮图片 - 使用与按钮组件相同的位置和尺寸
        drawButtonImages(g);
        // 绘制圆形头像
        drawCircle(g);
    }

    private void drawBackground(Graphics g) {
        try {
            ImageIcon bgIcon = new ImageIcon("APP/res/images/LoginBg.jpg");
            g.drawImage(bgIcon.getImage(), 0, 0, Const.WIDTH, Const.HEIGHT, this);
        } catch (Exception e) {
            // 如果背景图片加载失败，使用纯色背景
            g.setColor(new Color(240, 248, 255));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawButtonImages(Graphics g) {
        try {
            // 登录按钮图片 - 使用与按钮组件完全相同的位置和尺寸
            ImageIcon loginIcon = new ImageIcon(loginBtnPath);
            g.drawImage(loginIcon.getImage(),
                    LOGIN_BUTTON_X, BUTTONS_Y,
                    BUTTON_WIDTH, BUTTON_HEIGHT, this);

            // 注册按钮图片 - 使用与按钮组件完全相同的位置和尺寸
            ImageIcon registerIcon = new ImageIcon(registerBtnPath);
            g.drawImage(registerIcon.getImage(),
                    REGISTER_BUTTON_X, BUTTONS_Y,
                    BUTTON_WIDTH, BUTTON_HEIGHT, this);
        } catch (Exception e) {
            // 按钮图片加载失败时使用文字按钮
            drawFallbackButtons(g);
        }
    }

    private void drawFallbackButtons(Graphics g) {
        // 备用按钮绘制 - 确保与按钮组件位置一致
        g.setColor(new Color(0, 120, 215));

        // 登录按钮
        g.fillRect(LOGIN_BUTTON_X, BUTTONS_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.BOLD, 14));
        g.drawString("登录", LOGIN_BUTTON_X + 30, BUTTONS_Y + 20);

        // 注册按钮
        g.setColor(new Color(0, 120, 215));
        g.fillRect(REGISTER_BUTTON_X, BUTTONS_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("注册", REGISTER_BUTTON_X + 30, BUTTONS_Y + 20);
    }

    public void drawCircle(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Shape circle = new Ellipse2D.Double(125, 60, 130, 130);

        try {
            BufferedImage originalImage = ImageIO.read(new File("APP/res/images/头像1.png"));
            if (originalImage == null) {
                drawDefaultAvatar(g2d, circle);
                return;
            }

            // 缩放图片
            BufferedImage scaledImage = new BufferedImage(130, 130, BufferedImage.TYPE_INT_ARGB);
            Graphics2D scaleG2d = scaledImage.createGraphics();
            scaleG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            scaleG2d.drawImage(originalImage, 0, 0, 130, 130, null);
            scaleG2d.dispose();

            // 绘制圆形头像
            g2d.setClip(circle);
            g2d.drawImage(scaledImage, 125, 60, null);

        } catch (IOException e) {
            drawDefaultAvatar(g2d, circle);
        } finally {
            g2d.setClip(null);
        }
    }

    private void drawDefaultAvatar(Graphics2D g2d, Shape circle) {
        g2d.setClip(circle);
        g2d.setColor(new Color(200, 200, 200));
        g2d.fill(circle);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g2d.drawString("头像", 175, 125);
    }

    // 使得按钮变得透明的方法
    private void hideButton(JButton button) {
        button.setText("");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);

        // 确保按钮可以接收鼠标事件
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // 调试方法：显示按钮边界（开发完成后可移除）
    private void debugDrawButtonBounds(Graphics g) {
        g.setColor(Color.RED);
        g.drawRect(LOGIN_BUTTON_X, BUTTONS_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawRect(REGISTER_BUTTON_X, BUTTONS_Y, BUTTON_WIDTH, BUTTON_HEIGHT);

        // 在paintComponent方法末尾调用此方法进行调试
        // debugDrawButtonBounds(g);
    }
    public String getAccount(){
        return accountField.getText().trim();
    }
    public String getPassword(){
        return new String(passwordField.getPassword()).trim();
    }
}