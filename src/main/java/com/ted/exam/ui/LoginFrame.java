package com.ted.exam.ui;

import com.ted.exam.session.LoginSession;
import com.ted.exam.ui.components.RoundedButton;
import com.ted.exam.util.ApiUtil;
import com.ted.exam.model.common.ApiResponse.ExamNumberOption;
import com.ted.exam.model.common.ApiResponse.ExamNumberItem;
import com.ted.exam.model.resp.LoginVO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 考试客户端登录窗口（参考设计稿：双栏卡片 + 身份证登录）
 * 兼容 Windows XP/7 及以上系统
 */
public class LoginFrame extends JFrame {

    /** 主色 #0052D9 */
    private static final Color PRIMARY_BLUE = new Color(0, 82, 217);
    /** 左侧浅蓝背景 #EDF5FF（设计稿） */
    private static final Color LEFT_PANEL_BG = new Color(237, 245, 255);
    /** 背景装饰圆 */
    private static final Color DECO_CIRCLE = new Color(240, 245, 255);
    /** 输入框边框 #DCDCDC */
    private static final Color INPUT_BORDER = new Color(220, 220, 220);
    /** 正文灰 */
    private static final Color TEXT_BODY = new Color(68, 68, 68);
    /** 页面背景渐变（顶） */
    private static final Color PAGE_BG_TOP = new Color(245, 249, 255);
    /** 页面背景渐变（底） */
    private static final Color PAGE_BG_BOTTOM = new Color(252, 253, 255);
    /** 输入区浅底 */
    private static final Color FIELD_BG = new Color(248, 250, 252);
    /** 卡片描边 */
    private static final Color CARD_STROKE = new Color(226, 232, 240);
    /** 分栏竖线 */
    private static final Color COLUMN_RULE = new Color(228, 234, 245);

    private JTextField idNumberField;
    private RoundedButton enterExamButton;

    private JPanel rootContent;
    private boolean darkMode;

    private static final String CONFIG_FILE = "config.properties";
    private static final String KEY_ID_NUMBER = "idNumber";

    public LoginFrame() {
        initFrame();
        initComponents();
        loadSavedId();
    }

    private void initFrame() {
        setTitle("北京市特种设备作业人员考试系统-考试端 - 登录");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // 禁止 Ctrl+Tab / Ctrl+Shift+Tab / Alt+Tab 等切换
        setFocusTraversalKeysEnabled(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 拦截 Ctrl+Tab、Ctrl+Shift+Tab、Alt+Tab 等窗口切换快捷键
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ctrl TAB"), "block");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ctrl shift TAB"), "block");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ctrl pressed TAB"), "block");
        getRootPane().getActionMap().put("block", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        DecoratedBackgroundPanel background = new DecoratedBackgroundPanel();
        background.setLayout(new BorderLayout());

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrap.add(createLoginCard(), gbc);
        background.add(centerWrap, BorderLayout.CENTER);

        rootContent = background;
        applyThemeColors();
        add(background, BorderLayout.CENTER);
    }

    private JButton createIconButton(String text, String tip) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(4, 8, 4, 8));
        b.setForeground(new Color(120, 120, 120));
        b.setToolTipText(tip);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(PRIMARY_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(new Color(120, 120, 120));
            }
        });
        return b;
    }

    private void toggleFullscreen() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            setSize(1024, 680);
            setLocationRelativeTo(null);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        applyThemeColors();
        repaint();
    }

    private void applyThemeColors() {
        if (rootContent instanceof DecoratedBackgroundPanel) {
            ((DecoratedBackgroundPanel) rootContent).setDarkMode(darkMode);
        }
    }

    private JComponent createLoginCard() {
        ShadowCardPanel card = new ShadowCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(ShadowCardPanel.SHADOW, ShadowCardPanel.SHADOW,
                ShadowCardPanel.SHADOW, ShadowCardPanel.SHADOW));

        // 左说明 / 右登录：左侧略窄、右侧略宽
        JPanel twoCol = new JPanel(new GridBagLayout());
        twoCol.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.44;
        twoCol.add(createLeftInfoPanel(), c);
        c.gridx = 1;
        c.weightx = 0.56;
        twoCol.add(createRightFormPanel(), c);
        card.add(twoCol, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLeftInfoPanel() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(LEFT_PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, COLUMN_RULE),
                new EmptyBorder(36, 32, 36, 28)));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("欢迎参加本次考试");
        title.setFont(uiFont(Font.BOLD, 24));
        title.setForeground(PRIMARY_BLUE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        String subHtml = "<html><body style='width:340px;color:#" + toHex(new Color(85, 90, 102))
                + ";font-size:13px;line-height:1.7;'>"
                + "请使用身份证登录考试系统，选择本场考试的准考证开始考试。"
                + "</body></html>";
        JLabel sub = new JLabel(subHtml);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(14));
        p.add(sub);
        p.add(Box.createVerticalStrut(28));

        String[] items = {
                "身份验证入场，杜绝替考，保障考试公平",
                "实时监控考试状态，全程防作弊护航",
                "答题进度自动保存，意外中断不丢失作答内容",
                "考试时长倒计时提醒，合理分配答题时间",
                "交卷后即时出分，清晰知晓考试结果"
        };
        for (String line : items) {
            p.add(createFeatureRow(line));
            p.add(Box.createVerticalStrut(14));
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    /** 设计稿：左侧列表前为蓝底白勾小圆标 */
    private JPanel createFeatureRow(String text) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));

        JPanel bulletWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bulletWrap.setOpaque(false);
        bulletWrap.add(new BlueCircleCheckBullet());

        String html = "<html><body style='width:320px;color:#" + toHex(TEXT_BODY)
                + ";font-size:14px;line-height:1.55;'>" + escapeHtml(text) + "</body></html>";
        JLabel lab = new JLabel(html);
        row.add(bulletWrap, BorderLayout.WEST);
        row.add(lab, BorderLayout.CENTER);
        return row;
    }

    private static String toHex(Color c) {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** 「考生登录」标题下圆角主色条 */
    private static final class TabAccentBar extends JComponent {
        private static final int BAR_W = 88;
        private static final int BAR_H = 4;

        TabAccentBar() {
            setOpaque(false);
            Dimension d = new Dimension(BAR_W, BAR_H);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(0, 100, 230), w, 0, PRIMARY_BLUE));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            g2.dispose();
        }
    }

    /** 主色圆 + 白色对勾，与设计稿一致 */
    private static final class BlueCircleCheckBullet extends JComponent {
        private static final int D = 14;

        BlueCircleCheckBullet() {
            setOpaque(false);
            setPreferredSize(new Dimension(D + 4, 26));
            setMinimumSize(new Dimension(D + 4, 26));
            setMaximumSize(new Dimension(D + 4, 26));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = (getHeight() - D) / 2;
            g2.setColor(PRIMARY_BLUE);
            g2.fill(new Ellipse2D.Float(0, y, D, D));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D check = new Path2D.Float();
            check.moveTo(3.2f, y + 7.8f);
            check.lineTo(6.1f, y + 10.5f);
            check.lineTo(11f, y + 4.2f);
            g2.draw(check);
            g2.dispose();
        }
    }

    private JPanel createRightFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(true);
        outer.setBackground(Color.WHITE);

        // 内部用 BoxLayout 垂直排列，加 Box.createHorizontalGlue() 限制最大宽度
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // 与设计稿：表单略偏上（顶距不宜过大）
        p.add(Box.createVerticalStrut(32));

        JPanel tabHeader = new JPanel();
        tabHeader.setLayout(new BoxLayout(tabHeader, BoxLayout.Y_AXIS));
        tabHeader.setOpaque(false);
        tabHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tabTitle = new JLabel("考生登录");
        tabTitle.setFont(uiFont(Font.BOLD, 18));
        tabTitle.setForeground(PRIMARY_BLUE);
        tabTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComponent accent = new TabAccentBar();
        accent.setAlignmentX(Component.CENTER_ALIGNMENT);

        tabHeader.add(tabTitle);
        tabHeader.add(Box.createVerticalStrut(10));
        tabHeader.add(accent);

        p.add(tabHeader);
        p.add(Box.createVerticalStrut(28));

        idNumberField = createIdTextField("请输入身份证号");
        idNumberField.setFont(uiFont(Font.PLAIN, 15));
        idNumberField.setMaximumSize(new Dimension(320, 44));
        idNumberField.setPreferredSize(new Dimension(320, 44));
        idNumberField.setAlignmentX(Component.CENTER_ALIGNMENT);
        idNumberField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        enterExamButton = new RoundedButton("进入考试", 8);
        enterExamButton.setThemeColor(
                PRIMARY_BLUE,
                new Color(20, 100, 230),
                new Color(0, 60, 180)
        );
        enterExamButton.setFont(uiFont(Font.BOLD, 16));
        enterExamButton.setMaximumSize(new Dimension(320, 44));
        enterExamButton.setPreferredSize(new Dimension(320, 44));
        enterExamButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterExamButton.addActionListener(e -> performLogin());

        p.add(idNumberField);
        p.add(Box.createVerticalStrut(8));
        JLabel hint = new JLabel("支持 18 位二代居民身份证");
        hint.setFont(uiFont(Font.PLAIN, 12));
        hint.setForeground(new Color(148, 163, 184));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(hint);
        p.add(Box.createVerticalStrut(16));
        p.add(enterExamButton);

        p.add(Box.createVerticalGlue());

        // 限制最大宽度 + 居中
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        wrapper.add(Box.createHorizontalGlue(), BorderLayout.WEST);
        wrapper.add(p, BorderLayout.CENTER);

        outer.add(wrapper, BorderLayout.CENTER);
        return outer;
    }

    private JTextField createIdTextField(String placeholder) {
        final Color borderNormal = INPUT_BORDER;
        final Color borderFocus = PRIMARY_BLUE;

        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(156, 163, 175));
                    g2.setFont(getFont().deriveFont(Font.PLAIN));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, 14, y);
                    g2.dispose();
                }
            }
        };

        Runnable applyBorder = () -> {
            boolean f = field.hasFocus();
            field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(f ? borderFocus : borderNormal, f ? 2 : 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));
        };

        field.setFont(uiFont(Font.PLAIN, 15));
        field.setBackground(FIELD_BG);
        field.setCaretColor(PRIMARY_BLUE);
        field.setSelectionColor(new Color(0, 82, 217, 35));
        field.setSelectedTextColor(TEXT_BODY);
        applyBorder.run();
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                applyBorder.run();
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                applyBorder.run();
                field.repaint();
            }
        });
        return field;
    }

    private Font uiFont(int style, int size) {
        String[] prefer = {"Microsoft YaHei UI", "Microsoft YaHei", "SimSun"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String fam : prefer) {
            for (String a : available) {
                if (a.equals(fam)) {
                    return new Font(fam, style, size);
                }
            }
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    private ImageIcon createBrandIcon() {
        int size = 36;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PRIMARY_BLUE);
        g2.fillOval(2, 2, size - 4, size - 4);
        g2.setColor(new Color(255, 80, 80));
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(10, 12, 16, 12, 200, 140);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void performLogin() {
        String id = idNumberField.getText().trim();
        if (id.isEmpty()) {
            showDialogAutoClose("请输入身份证号", "提示", JOptionPane.WARNING_MESSAGE, 2000);
            idNumberField.requestFocus();
            return;
        }

        if (!ApiUtil.isValidIdCardFormat(id)) {
            showDialogAutoClose("身份证格式不正确，请检查后重新输入", "格式错误", JOptionPane.WARNING_MESSAGE, 2000);
            idNumberField.requestFocus();
            return;
        }

        enterExamButton.setEnabled(false);
        enterExamButton.setText("验证中...");
        enterExamButton.repaint();

        new SwingWorker<List<ExamNumberOption>, Void>() {
            @Override
            protected List<ExamNumberOption> doInBackground() {
                String encryptedId = ApiUtil.encryptByRsa(id);
                return ApiUtil.getExamNumbersByIdCard(encryptedId);
            }

            @Override
            protected void done() {
                try {
                    List<ExamNumberOption> options = get();
                    if (options == null || options.isEmpty()) {
                        showDialogAutoClose("未查询到该身份证对应的考试信息", "查询结果", JOptionPane.INFORMATION_MESSAGE, 2500);
                        resetButton();
                        return;
                    }
                    // 收集所有准考证
                    List<String> allExamNumbers = new ArrayList<>();
                    for (ExamNumberOption opt : options) {
                        if (opt.getChildren() != null) {
                            for (ExamNumberItem item : opt.getChildren()) {
                                allExamNumbers.add(item.getValueAsString());
                            }
                        }
                    }
                    if (allExamNumbers.size() == 1) {
                        doSubmitLogin(id, allExamNumbers.get(0));
                    } else {
                        String selected = showExamNumberDialog(options);
                        if (selected != null && !selected.isEmpty()) {
                            doSubmitLogin(id, selected);
                        } else {
                            resetButton();
                        }
                    }
                } catch (Exception ex) {
                    showDialogAutoClose(userFacingMessage(ex), "提示", JOptionPane.ERROR_MESSAGE, 3000);
                    resetButton();
                }
            }
        }.execute();
    }

    private void doSubmitLogin(String id, String examNumber) {
        new SwingWorker<LoginVO, Void>() {
            @Override
            protected LoginVO doInBackground() {
                String encryptedId = ApiUtil.encryptByRsa(id);
                String encryptedExamNumber = ApiUtil.encryptByRsa(examNumber);
                String encryptedPassword = ApiUtil.encryptByRsa("EXAM-LOGIN");
                return ApiUtil.submitLogin(encryptedId, encryptedExamNumber, encryptedPassword);
            }

            @Override
            protected void done() {
                try {
                    LoginVO resp = get();
                    if (resp != null) {
                        LoginSession.get().login(resp);
                        saveId(id);
                        Timer timer = new Timer(400, e -> openMainInterface());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showDialogAutoClose("登录失败，请重试", "登录失败", JOptionPane.ERROR_MESSAGE, 2500);
                        resetButton();
                    }
                } catch (Exception ex) {
                    showDialogAutoClose(userFacingMessage(ex), "登录失败", JOptionPane.ERROR_MESSAGE, 3000);
                    resetButton();
                }
            }
        }.execute();
    }

    /**
     * SwingWorker.get() 会把后台异常包装成 ExecutionException，其 message 常带类名；
     * 只展示内层业务信息（如接口返回的 msg）。
     */
    private static String userFacingMessage(Throwable ex) {
        Throwable t = ex;
        while (t instanceof ExecutionException && t.getCause() != null) {
            t = t.getCause();
        }
        String m = t.getMessage();
        if (m != null && !m.isEmpty()) {
            return m;
        }
        return "操作失败";
    }

    private String showExamNumberDialog(List<ExamNumberOption> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_STROKE, 1),
                new EmptyBorder(16, 18, 16, 18)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label1 = new JLabel("考试场次");
        label1.setFont(uiFont(Font.BOLD, 13));
        label1.setForeground(TEXT_BODY);
        JComboBox<ExamNumberOption> planCombo = new JComboBox<>();
        for (ExamNumberOption opt : options) {
            planCombo.addItem(opt);
        }
        planCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ExamNumberOption) {
                    setText(((ExamNumberOption) value).getLabel());
                }
                return c;
            }
        });
        planCombo.setFont(uiFont(Font.PLAIN, 13));
        styleLoginCombo(planCombo);

        JLabel label2 = new JLabel("准考证号");
        label2.setFont(uiFont(Font.BOLD, 13));
        label2.setForeground(TEXT_BODY);
        JComboBox<String> examCombo = new JComboBox<>();
        examCombo.setFont(uiFont(Font.PLAIN, 13));
        styleLoginCombo(examCombo);

        List<ExamNumberItem> currentChildren = options.isEmpty() ? null : options.get(0).getChildren();
        if (currentChildren != null) {
            for (ExamNumberItem item : currentChildren) {
                examCombo.addItem(item.getValueAsString());
            }
        }

        planCombo.addActionListener(e -> {
            ExamNumberOption selected = (ExamNumberOption) planCombo.getSelectedItem();
            examCombo.removeAllItems();
            if (selected != null && selected.getChildren() != null) {
                for (ExamNumberItem item : selected.getChildren()) {
                    examCombo.addItem(item.getValueAsString());
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(label1, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        panel.add(planCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(label2, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        panel.add(examCombo, gbc);

        int result = JOptionPane.showConfirmDialog(this,
                panel, "请选择考试场次和准考证", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Object selected = examCombo.getSelectedItem();
            if (selected != null) {
                return selected.toString().trim();
            }
        }
        return null;
    }

    private void resetButton() {
        enterExamButton.setText("进入考试");
        enterExamButton.setEnabled(true);
        enterExamButton.repaint();
    }

    private void styleLoginCombo(JComponent combo) {
        combo.setBackground(FIELD_BG);
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(INPUT_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        if (combo instanceof JComboBox) {
            ((JComboBox<?>) combo).setPreferredSize(new Dimension(280, 36));
        }
    }

    private void showDialogAutoClose(String message, String title, int messageType, int delayMs) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel topStripe = new JPanel();
        topStripe.setPreferredSize(new Dimension(10, 4));
        topStripe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        topStripe.setBackground(PRIMARY_BLUE);
        topStripe.setOpaque(true);

        JLabel label = new JLabel("<html><div style='text-align:center;width:280px;'>" + escapeHtml(message) + "</div></html>",
                SwingConstants.CENTER);
        label.setFont(uiFont(Font.PLAIN, 14));
        label.setForeground(TEXT_BODY);
        label.setBorder(new EmptyBorder(22, 28, 24, 28));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(label, BorderLayout.CENTER);

        dialog.add(topStripe, BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);

        dialog.pack();
        dialog.setMinimumSize(dialog.getPreferredSize());
        dialog.setLocationRelativeTo(this);

        new Timer(delayMs, e -> {
            dialog.dispose();
            ((Timer) e.getSource()).stop();
        }).start();

        dialog.setVisible(true);
    }

    private void openMainInterface() {
        dispose();
        HomeFrame home = new HomeFrame();
        home.setVisible(true);
    }

    private void saveId(String id) {
        try {
            Properties props = new Properties();
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileInputStream in = new FileInputStream(f)) {
                    props.load(in);
                }
            }
            props.setProperty(KEY_ID_NUMBER, id);
            try (FileOutputStream out = new FileOutputStream(f)) {
                props.store(out, "TED Exam Client");
            }
        } catch (IOException e) {
            System.err.println("保存失败: " + e.getMessage());
        }
    }

    private void loadSavedId() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) {
            return;
        }
        try (FileInputStream in = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(in);
            String id = props.getProperty(KEY_ID_NUMBER, "");
            if (!id.isEmpty()) {
                idNumberField.setText(id);
            }
        } catch (IOException e) {
            System.err.println("加载失败: " + e.getMessage());
        }
    }

    private class DecoratedBackgroundPanel extends JPanel {
        private boolean dark;

        void setDarkMode(boolean dark) {
            this.dark = dark;
            setBackground(dark ? new Color(30, 32, 38) : Color.WHITE);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (!dark) {
                GradientPaint gp = new GradientPaint(0, 0, PAGE_BG_TOP, w, h, PAGE_BG_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
            }

            float r = Math.min(w, h) * 0.35f;
            Color c1 = dark ? new Color(45, 55, 75, 200) : new Color(224, 234, 255, 90);
            Color c2 = dark ? new Color(40, 50, 68, 160) : new Color(237, 245, 255, 120);
            g2.setColor(c1);
            g2.fill(new Ellipse2D.Float(-r * 0.4f, -r * 0.3f, r, r));
            g2.setColor(c2);
            g2.fill(new Ellipse2D.Float(w - r * 0.7f, -r * 0.2f, r * 0.9f, r * 0.9f));
            g2.setColor(c1);
            g2.fill(new Ellipse2D.Float(-r * 0.2f, h - r * 0.6f, r * 0.85f, r * 0.85f));
            g2.setColor(c2);
            g2.fill(new Ellipse2D.Float(w - r * 0.55f, h - r * 0.55f, r * 0.75f, r * 0.75f));
            g2.dispose();
        }
    }

    private static class ShadowCardPanel extends JPanel {
        private static final int RADIUS = 14;
        static final int SHADOW = 10;

        ShadowCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = SHADOW;
            int y = SHADOW;
            int w = getWidth() - SHADOW * 2;
            int h = getHeight() - SHADOW * 2;

            g2.setColor(new Color(15, 23, 42, 10));
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 8, w, h, RADIUS + 3, RADIUS + 3));
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fill(new RoundRectangle2D.Float(x + 2, y + 4, w, h, RADIUS + 2, RADIUS + 2));

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, RADIUS, RADIUS));

            g2.setColor(CARD_STROKE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, RADIUS, RADIUS));
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(880, 540);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(680, 460);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(920, Integer.MAX_VALUE);
        }
    }
}
