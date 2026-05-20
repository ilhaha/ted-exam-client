package com.ted.exam.ui;

import com.ted.exam.session.LoginSession;
import com.ted.exam.util.ApiUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 交卷成功结束页：展示提示与得分，倒计时结束后返回登录。
 */
public class ExamEndFrame extends JFrame {

    private static final int REDIRECT_SECONDS = 15;

    private static final Color PAGE_BG = new Color(224, 242, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TITLE_TEAL = new Color(13, 148, 136);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_GRAY = new Color(71, 85, 105);
    private static final Color SCORE_RED = new Color(220, 38, 38);
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);

    private static final float FS_TITLE = 22f;
    private static final float FS_BODY = 15f;
    private static final float FS_SCORE = 26f;
    private static final float FS_COUNTDOWN = 14f;

    private javax.swing.Timer redirectTimer;
    private int remainingSeconds = REDIRECT_SECONDS;
    private JLabel countdownLabel;
    private final AtomicBoolean returnedToLogin = new AtomicBoolean(false);

    private static Font uiFont(int style, float sizePx) {
        Font base = new Font("Microsoft YaHei UI", style, 14);
        return base.deriveFont(sizePx);
    }

    /**
     * @param finalScore   得分（答对题数）
     * @param totalScore   试卷总题数
     * @param recordHint   提交记录相关提示，可为 null
     */
    public ExamEndFrame(int finalScore, int totalScore, String recordHint) {
        initFrame();
        setContentPane(buildRoot(finalScore, totalScore, recordHint));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToLoginOnce();
            }
        });
        startRedirectTimer();
    }

    private void initFrame() {
        setTitle("答卷已提交");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void addTitleBar(JPanel root) {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(13, 148, 136));
        titleBar.setPreferredSize(new Dimension(0, 44));

        JLabel title = new JLabel("答卷已提交", SwingConstants.CENTER);
        title.setFont(uiFont(Font.PLAIN, 15f));
        title.setForeground(Color.WHITE);
        titleBar.add(title, BorderLayout.CENTER);

        root.add(titleBar, BorderLayout.NORTH);
    }

    private JPanel buildRoot(int finalScore, int totalScore, String recordHint) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);

        addTitleBar(root);

        JPanel cardWrap = new JPanel(new GridBagLayout());
        cardWrap.setOpaque(false);

        RoundedCardPanel card = new RoundedCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 40, 32, 40));
        card.setOpaque(false);

        JLabel head = new JLabel("答卷已提交成功");
        head.setFont(uiFont(Font.BOLD, FS_TITLE));
        head.setForeground(TITLE_TEAL);
        head.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(head);
        card.add(Box.createVerticalStrut(24));

        String[] tips = new String[]{
                "请前往监考员处确认答卷提交状态",
                "保持考场秩序，请勿喧哗或逗留",
                "检查随身物品是否携带完整",
                "如对考试内容有异议，可通过正式渠道申诉"
        };
        for (String tip : tips) {
            card.add(bulletLine(tip));
            card.add(Box.createVerticalStrut(10));
        }

        if (recordHint != null && !recordHint.isEmpty()) {
            card.add(Box.createVerticalStrut(6));
            JLabel warn = new JLabel("<html><div style='width:420px;color:#b45309;'>" + escapeHtml(recordHint) + "</div></html>");
            warn.setFont(uiFont(Font.PLAIN, FS_BODY - 1f));
            warn.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(warn);
        }

        card.add(Box.createVerticalStrut(20));

        boolean pass = totalScore <= 0 || (finalScore * 100.0 / totalScore) >= 70.0;
        String passText = pass ? "及格" : "不及格";
        Color passColor = pass ? new Color(22, 163, 74) : SCORE_RED;

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        scoreRow.setOpaque(false);
        JLabel s1 = new JLabel("本次考试得分：");
        s1.setFont(uiFont(Font.BOLD, FS_SCORE));
        s1.setForeground(TEXT_DARK);
        JLabel s2 = new JLabel(String.valueOf(finalScore));
        s2.setFont(uiFont(Font.BOLD, FS_SCORE + 6f));
        s2.setForeground(SCORE_RED);
        JLabel s3 = new JLabel(" / " + totalScore + "  ");
        s3.setFont(uiFont(Font.BOLD, FS_SCORE));
        s3.setForeground(TEXT_DARK);
        JLabel s4 = new JLabel("(" + passText + ")");
        s4.setFont(uiFont(Font.BOLD, FS_SCORE));
        s4.setForeground(passColor);
        scoreRow.add(s1);
        scoreRow.add(s2);
        scoreRow.add(s3);
        scoreRow.add(s4);
        scoreRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(scoreRow);

        card.add(Box.createVerticalStrut(28));

        countdownLabel = new JLabel();
        countdownLabel.setFont(uiFont(Font.PLAIN, FS_COUNTDOWN));
        countdownLabel.setForeground(TEXT_GRAY);
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateCountdownText();
        card.add(countdownLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        cardWrap.add(card, gbc);

        root.add(cardWrap, BorderLayout.CENTER);

        return root;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private JPanel bulletLine(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(480, 40));

        JLabel dot = new JLabel("•");
        dot.setFont(uiFont(Font.BOLD, FS_BODY));
        dot.setForeground(TITLE_TEAL);

        JLabel t = new JLabel(text);
        t.setFont(uiFont(Font.PLAIN, FS_BODY));
        t.setForeground(TEXT_DARK);

        row.add(dot);
        row.add(t);
        return row;
    }

    private void updateCountdownText() {
        countdownLabel.setText(remainingSeconds + " 秒后自动返回登录页面");
    }

    private void startRedirectTimer() {
        redirectTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                redirectTimer.stop();
                returnToLoginOnce();
            } else {
                updateCountdownText();
            }
        });
        redirectTimer.start();
    }

    private void returnToLoginOnce() {
        if (!returnedToLogin.compareAndSet(false, true)) {
            return;
        }
        if (redirectTimer != null) {
            redirectTimer.stop();
            redirectTimer = null;
        }

        // 先调用退出登录接口，再清理本地会话
        new Thread(() -> ApiUtil.logout()).start();
        LoginSession.get().logout();
        ApiUtil.clearAuthToken();

        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }

    /** 圆角白底卡片（带浅阴影） */
    private static final class RoundedCardPanel extends JPanel {
        private static final int R = 18;
        private static final int SH = 6;

        RoundedCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = SH;
            int y = SH;
            int w = getWidth() - SH * 2;
            int h = getHeight() - SH * 2;
            if (w < 4 || h < 4) {
                g2.dispose();
                return;
            }
            g2.setColor(new Color(15, 23, 42, 8));
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 4, w, h, R + 2, R + 2));
            g2.setColor(CARD_BG);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, R, R));
            g2.setColor(BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, R, R));
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(520, 420);
        }

    }
}
