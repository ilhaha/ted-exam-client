package com.ted.exam.ui;


import com.ted.exam.model.resp.ExamCandidateInfoVO;
import com.ted.exam.model.resp.ExamPaperVO;
import com.ted.exam.model.resp.UserInfoVO;
import com.ted.exam.session.LoginSession;
import com.ted.exam.util.ApiUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * 考试客户端首页（登录成功后展示）
 */
public class HomeFrame extends JFrame {

    /** 主色：偏现代的靛蓝 */
    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color PRIMARY_BLUE_HOVER = new Color(29, 78, 216);
    private static final Color PRIMARY_SOFT_BG = new Color(239, 246, 255);
    private static final Color PAGE_BG = new Color(241, 245, 249);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color LINE_GRAY = new Color(226, 232, 240);

    private static final int AVATAR_SIZE = 120;
    private static final int BODY_MIN_HEIGHT = 460;

    private JPanel rootContent;
    private JLabel nameValueLabel;
    private JLabel idCardValueLabel;
    private JLabel ticketValueLabel;
    private JLabel subjectValueLabel;
    private JLabel venueValueLabel;
    private JLabel timeValueLabel;
    private JLabel avatarLabel;
    private JPanel avatarWrap;
    private JLabel clockLabel;
    private Timer clockTimer;
    private boolean shortFilmWatched = false;
    private JButton enterExamBtn;

    private static Font uiFont(int style, float sizePx) {
        Font base = new Font("Microsoft YaHei UI", style, 14);
        return base.deriveFont(sizePx);
    }

    /** 统一字号常量 */
    private static final float FS_TITLE   = 22f;
    private static final float FS_SECTION = 20f;
    private static final float FS_LABEL   = 15f;
    private static final float FS_VALUE   = 17f;
    private static final float FS_BTN     = 19f;
    private static final float FS_HINT    = 16f;

    private static Font uiFontPlain(float sizePx) {
        return uiFont(Font.PLAIN, sizePx);
    }

    private JButton createGhostTextButton(String text, Runnable action) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover() && getModel().isEnabled()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        b.setFont(uiFontPlain(text.equals("退出") ? FS_LABEL + 3 : FS_LABEL));
        b.setForeground(text.equals("退出") ? new Color(220, 38, 38) : TEXT_GRAY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setRolloverEnabled(true);
        b.addActionListener(e -> action.run());
        return b;
    }

    public HomeFrame() {
        initFrame();
        initComponents();
        loadUserInfo();
    }

    private void initFrame() {
        setTitle("北京市特种设备作业人员考试系统-考试端 - 确认考试信息");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
            clockTimer = null;
        }
        super.dispose();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel background = new LightGrayBackgroundPanel();
        background.setLayout(new BorderLayout());

        background.add(buildTitleBar(), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        GridBagConstraints cw = new GridBagConstraints();
        cw.gridx = 0;
        cw.gridy = 0;
        cw.weightx = 1;
        cw.weighty = 1;
        cw.fill = GridBagConstraints.BOTH;
        cw.insets = new Insets(20, 40, 24, 40);
        JPanel cardSlot = new JPanel(new GridBagLayout());
        cardSlot.setOpaque(false);
        GridBagConstraints slot = new GridBagConstraints();
        slot.gridx = 0;
        slot.gridy = 0;
        slot.weightx = 1;
        slot.weighty = 1;
        slot.anchor = GridBagConstraints.CENTER;
        slot.fill = GridBagConstraints.HORIZONTAL;
        cardSlot.add(buildMainCard(), slot);
        centerWrap.add(cardSlot, cw);

        background.add(centerWrap, BorderLayout.CENTER);
        rootContent = background;
        add(background);

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();
    }

    private JComponent buildMainCard() {
        MainShadowCardPanel card = new MainShadowCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        int s = MainShadowCardPanel.SHADOW;
        card.setBorder(new EmptyBorder(s + 32, s + 40, s + 36, s + 40));

        JComponent header = createCardHeader();
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(header);
        card.add(Box.createVerticalStrut(28));

        JComponent body = createTwoColumnBody();
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(body);
        card.add(Box.createVerticalStrut(32));

        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        footerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerRow.add(createCardFooter(), BorderLayout.CENTER);
        int fh = footerRow.getPreferredSize().height;
        footerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, fh));
        card.add(footerRow);

        return card;
    }

    private JComponent createCardHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel pageTitle = new JLabel("确认信息并开始考试", SwingConstants.CENTER);
        pageTitle.setFont(uiFont(Font.BOLD, FS_TITLE));
        pageTitle.setForeground(PRIMARY_BLUE);

        clockLabel = new JLabel();
        clockLabel.setFont(uiFont(Font.PLAIN, FS_HINT));
        clockLabel.setForeground(TEXT_MUTED);

        JButton logoutBtn = createGhostTextButton("退出", this::doLogout);

        JPanel east = new JPanel();
        east.setOpaque(false);
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.add(logoutBtn);
        east.add(Box.createVerticalStrut(6));
        JPanel clockRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        clockRow.setOpaque(false);
        clockRow.add(clockLabel);
        east.add(clockRow);

        JPanel rightPad = new JPanel(new BorderLayout());
        rightPad.setOpaque(false);
        rightPad.add(east, BorderLayout.EAST);

        JPanel leftPad = new JPanel();
        leftPad.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        header.add(leftPad, gc);

        gc.gridx = 1;
        gc.weightx = 0;
        header.add(pageTitle, gc);

        gc.gridx = 2;
        gc.weightx = 1;
        header.add(rightPad, gc);

        return header;
    }

    private void updateClock() {
        if (clockLabel != null) {
            clockLabel.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date()));
        }
    }

    private JComponent buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY_BLUE);
        bar.setPreferredSize(new Dimension(0, 44));

        JLabel title = new JLabel("北京市特种设备作业人员考试系统-考试端 - 确认考试信息", SwingConstants.CENTER);
        title.setFont(uiFontPlain(FS_LABEL));
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.CENTER);

        return bar;
    }

    private JComponent createTwoColumnBody() {
        JPanel row = new JPanel(new GridBagLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = Math.max(d.height, BODY_MIN_HEIGHT);
                return d;
            }

            @Override
            public Dimension getMinimumSize() {
                Dimension d = super.getMinimumSize();
                d.height = Math.max(d.height, BODY_MIN_HEIGHT);
                return d;
            }
        };
        row.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        c.weightx = 0.44;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        row.add(createPersonalInfoColumn(), c);

        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(12, 28, 12, 28);
        row.add(columnDivider(), c);

        c.gridx = 2;
        c.weightx = 0.56;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        row.add(createAnnouncementColumn(), c);

        return row;
    }

    private JComponent columnDivider() {
        JComponent line = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LINE_GRAY);
                int x = getWidth() / 2;
                g2.fillRect(x, 0, 1, getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1, BODY_MIN_HEIGHT - 40);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(1, 200);
            }
        };
        line.setOpaque(false);
        return line;
    }

    private JComponent createPersonalInfoColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 0));
        col.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(sectionTitle("个人信息"));
        titleBlock.add(sectionLine());
        col.add(titleBlock, BorderLayout.NORTH);

        avatarWrap = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = 14;
                g2.setColor(PRIMARY_SOFT_BG);
                g2.fillRoundRect(0, 0, AVATAR_SIZE, AVATAR_SIZE, r, r);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, AVATAR_SIZE - 1, AVATAR_SIZE - 1, r, r);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(AVATAR_SIZE, AVATAR_SIZE);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        avatarLabel = new SquareAvatarLabel();
        avatarLabel.setFont(uiFont(Font.BOLD, 40f));
        avatarLabel.setForeground(PRIMARY_BLUE);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setText(getInitials());
        avatarWrap.setLayout(new BorderLayout());
        avatarWrap.add(avatarLabel, BorderLayout.CENTER);
        avatarWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();

        nameValueLabel   = new JLabel("-");
        idCardValueLabel = new JLabel("-");
        ticketValueLabel = new JLabel(exam != null ? nullToDash(exam.getExamNumber()) : "-");
        subjectValueLabel  = new JLabel(exam != null ? nullToDash(exam.getPlanName()) : "-");
        venueValueLabel    = new JLabel(exam != null ? nullToDash(exam.getClassroomName()) : "-");
        timeValueLabel     = new JLabel(exam != null ? nullToDash(exam.getExamTime()) : "-");

        int fieldGap = 14;
        // 个人信息：姓名、身份证号、准考证号
        JPanel personalInfoGrid = new JPanel(new GridLayout(1, 3, fieldGap, fieldGap));
        personalInfoGrid.setOpaque(false);
        personalInfoGrid.add(makeFieldCard("考生姓名", nameValueLabel));
        personalInfoGrid.add(makeFieldCard("身份证号", idCardValueLabel));
        personalInfoGrid.add(makeFieldCard("准考证号", ticketValueLabel));

        // 考试信息：科目、考场、时间
        JPanel examInfoGrid = new JPanel(new GridLayout(1, 3, fieldGap, fieldGap));
        examInfoGrid.setOpaque(false);
        examInfoGrid.add(makeFieldCard("考试科目", subjectValueLabel));
        examInfoGrid.add(makeFieldCard("考试考场", venueValueLabel));
        examInfoGrid.add(makeFieldCard("考试时间", timeValueLabel));

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarRow.setOpaque(false);
        avatarRow.add(avatarWrap);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(avatarRow);
        rows.add(Box.createVerticalStrut(16));
        rows.add(personalInfoGrid);
        rows.add(Box.createVerticalStrut(fieldGap));
        rows.add(examInfoGrid);

        col.add(rows, BorderLayout.CENTER);
        return col;
    }

    private JPanel makeFieldCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 12, 14, 12));

        JLabel lbl = new JLabel(label);
        lbl.setFont(uiFontPlain(FS_LABEL));
        lbl.setForeground(TEXT_MUTED);
        lbl.setVerticalAlignment(SwingConstants.TOP);

        valueLabel.setFont(uiFont(Font.BOLD, FS_VALUE));
        valueLabel.setForeground(TEXT_DARK);
        valueLabel.setVerticalAlignment(SwingConstants.TOP);

        card.add(lbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JComponent createAnnouncementColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 0));
        col.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // top.add(sectionTitle("考试公告"));
        top.add(sectionLine());
        top.add(Box.createVerticalStrut(22));

        JLabel sub1 = new JLabel("考试须知");
        sub1.setFont(uiFont(Font.BOLD, FS_VALUE + 3));
        sub1.setForeground(PRIMARY_BLUE);
        sub1.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(sub1);
        top.add(Box.createVerticalStrut(16));

        ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();
        String durationPart = exam != null && exam.getExamDuration() != null
                ? exam.getExamDuration() + " 分钟"
                : "90 分钟";
        JTextArea notice = new JTextArea(
                "1. 考试时间为 " + durationPart + "，请合理安排答题时间。\n\n"
                        + "2. 考试期间请保持安静，遵守考场纪律。\n\n" 
                        + "3. 考试过程中若遇网络中断，请立即联系监考老师，切勿自行重启设备。\n\n");
        styleAnnouncementText(notice);
        notice.setColumns(36);
        top.add(wrapAnnouncement(notice, 120, 160));
        top.add(Box.createVerticalStrut(32));

        JLabel sub2 = new JLabel("注意事项");
        sub2.setFont(uiFont(Font.BOLD, FS_VALUE + 3));
        sub2.setForeground(PRIMARY_BLUE);
        sub2.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(sub2);
        top.add(Box.createVerticalStrut(16));

        JTextArea caution = new JTextArea(
                "1. 考试系统出现异常请及时联系监考老师。\n\n"
                        + "2. 【重要须知】考试作答时，严禁刷新页面、切换屏幕，违规将影响考试成绩。\n\n"
                        + "3. 作答过程中请勿随意切换输入法或调整系统设置，防止页面卡顿。\n\n"
                        + "4. 考试过程中若遇网络中断，请立即联系监考老师，切勿自行重启设备。\n\n"
                        + "5. 禁止使用任何辅助软件、工具书或通讯设备，一经发现按作弊处理。\n\n"
                        + "6. 交卷后请确认系统提示\"提交成功\"，再离开考场。\n\n");
        styleAnnouncementText(caution);
        caution.setColumns(36);
        top.add(wrapAnnouncement(caution, 300, 450));

        col.add(top, BorderLayout.NORTH);

        return col;
    }

    private static void styleAnnouncementText(JTextArea ta) {
        ta.setFont(uiFontPlain(FS_VALUE));
        ta.setForeground(TEXT_GRAY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setBorder(null);
        ta.setRows(0);
        ta.setMargin(new Insets(8, 0, 16, 0));
    }

    private static JScrollPane wrapAnnouncement(JTextArea ta, int minHeight, int maxHeight) {
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = ta.getPreferredSize();
        int h = Math.min(pref.height + 12, maxHeight);
        h = Math.max(h, minHeight);
        sp.setPreferredSize(new Dimension(pref.width + 4, h));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return sp;
    }

    private JLabel sectionTitle(String text) {
        JLabel t = new JLabel(text);
        t.setFont(uiFont(Font.BOLD, FS_SECTION));
        t.setForeground(PRIMARY_BLUE);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        return t;
    }

    private JComponent sectionLine() {
        JPanel line = new JPanel();
        line.setOpaque(false);
        line.setLayout(new BorderLayout());
        line.setBorder(new EmptyBorder(10, 0, 0, 0));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSeparator sep = new JSeparator();
        sep.setForeground(LINE_GRAY);
        sep.setBackground(LINE_GRAY);
        line.add(sep, BorderLayout.CENTER);
        return line;
    }

    private JComponent createCardFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));
        footer.add(createOutlineEnterButton());
        return footer;
    }

    private JButton createOutlineEnterButton() {
        enterExamBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = 12;
                RoundRectangle2D.Float rr = new RoundRectangle2D.Float(1.5f, 1.5f,
                        getWidth() - 3, getHeight() - 3, r, r);
                Color fill = Color.WHITE;
                if (getModel().isRollover() && getModel().isEnabled()) {
                    fill = PRIMARY_SOFT_BG;
                }
                if (getModel().isPressed()) {
                    fill = new Color(219, 234, 254);
                }
                g2.setColor(fill);
                g2.fill(rr);
                Color stroke = PRIMARY_BLUE;
                if (getModel().isPressed()) {
                    stroke = PRIMARY_BLUE_HOVER;
                } else if (getModel().isRollover()) {
                    stroke = PRIMARY_BLUE_HOVER;
                }
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(rr);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        enterExamBtn.setContentAreaFilled(false);
        enterExamBtn.setBorderPainted(false);
        enterExamBtn.setFocusPainted(false);
        enterExamBtn.setForeground(PRIMARY_BLUE);
        enterExamBtn.setFont(uiFont(Font.BOLD, FS_BTN));
        enterExamBtn.setMargin(new Insets(16, 72, 16, 72));
        enterExamBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enterExamBtn.setRolloverEnabled(true);
        updateEnterButtonText();
        enterExamBtn.addActionListener(e -> onEnterExamClick());
        return enterExamBtn;
    }

    private void updateEnterButtonText() {
        if (enterExamBtn == null) return;
        ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();
        String warningShortFilm = exam != null ? exam.getWarningShortFilm() : null;
        if (warningShortFilm != null && !warningShortFilm.isEmpty() && !shortFilmWatched) {
            enterExamBtn.setText("观看警示短片");
        } else {
            enterExamBtn.setText("进入考试");
        }
    }

    private void loadUserInfo() {
        new SwingWorker<UserInfoVO, Void>() {
            @Override
            protected UserInfoVO doInBackground() {
                return ApiUtil.getUserInfo();
            }

            @Override
            protected void done() {
                try {
                    UserInfoVO info = get();
                    if (info != null) {
                        LoginSession.get().setUserInfo(info);
                        nameValueLabel.setText(nullToDash(info.getNickname()));
                        idCardValueLabel.setText(nullToDash(info.getUsername()));

                        String facePhoto = info.getAvatar();
                        if (facePhoto != null && !facePhoto.isEmpty()) {
                            try {
                                String fullUrl = facePhoto.startsWith("http")
                                        ? facePhoto
                                        : ApiUtil.getBaseUrl() + facePhoto;
                                URL url = new URL(fullUrl);
                                ImageIcon icon = new ImageIcon(url);
                                Image scaled = icon.getImage().getScaledInstance(
                                        AVATAR_SIZE, AVATAR_SIZE, Image.SCALE_SMOOTH);
                                avatarLabel.setIcon(new ImageIcon(scaled));
                                avatarLabel.setText(null);
                            } catch (Exception ex) {
                                // 加载失败保留缩写
                            }
                        }
                    }
                    refreshRoot();
                    updateEnterButtonText();
                } catch (ExecutionException e) {
                    System.err.println("加载用户信息失败: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
    }

    private void refreshRoot() {
        if (rootContent != null) {
            rootContent.revalidate();
            rootContent.repaint();
        }
    }

    private static String nullToDash(String s) {
        return s != null && !s.isEmpty() ? s : "-";
    }

    private String getInitials() {
        String name = LoginSession.get().getNickname();
        if (name != null && !name.isEmpty()) {
            return name.substring(0, Math.min(2, name.length()));
        }
        ExamCandidateInfoVO examInfo = LoginSession.get().getExamInfo();
        if (examInfo != null && examInfo.getPlanName() != null) {
            return examInfo.getPlanName().substring(0, Math.min(2, examInfo.getPlanName().length()));
        }
        return "考";
    }

    private void onEnterExamClick() {
        ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();
        String warningShortFilm = exam != null ? exam.getWarningShortFilm() : null;
        if (warningShortFilm != null && !warningShortFilm.isEmpty() && !shortFilmWatched) {
            showWarningShortFilm(warningShortFilm);
        } else {
            showEnterExamConfirm();
        }
    }

    /**
     * 弹出"确认开始考试"对话框，用户确认后加载试卷并进入考试
     */
    private void showEnterExamConfirm() {
        JDialog confirmDialog = new JDialog(this, "确认开始考试", true);
        confirmDialog.setSize(460, 300);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setLayout(new BorderLayout());
        confirmDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(36, 44, 32, 44));
        content.setBackground(PAGE_BG);

        JLabel titleLabel = new JLabel("确认开始考试", SwingConstants.CENTER);
        titleLabel.setFont(uiFont(Font.BOLD, FS_TITLE));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));

        JLabel warnLabel = new JLabel("您确认要开始考试吗？", SwingConstants.CENTER);
        warnLabel.setFont(uiFont(Font.PLAIN, FS_HINT));
        warnLabel.setForeground(TEXT_GRAY);
        warnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(warnLabel);
        content.add(Box.createVerticalStrut(6));

        JLabel hintLabel = new JLabel("开始后不可退出考试，否则成绩无效！", SwingConstants.CENTER);
        hintLabel.setFont(uiFont(Font.BOLD, FS_HINT));
        hintLabel.setForeground(new Color(220, 38, 38));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(hintLabel);
        content.add(Box.createVerticalStrut(28));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);

        JButton cancelBtn = new JButton("取消") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(241, 245, 249);
                if (getModel().isRollover() && getModel().isEnabled()) {
                    bg = new Color(226, 232, 240);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(uiFont(Font.PLAIN, FS_VALUE));
        cancelBtn.setForeground(TEXT_GRAY);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setMargin(new Insets(12, 36, 12, 36));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setRolloverEnabled(true);

        JButton okBtn = new JButton("确认开始") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(220, 38, 38);
                if (getModel().isRollover() && getModel().isEnabled()) {
                    bg = new Color(185, 28, 28);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        okBtn.setFont(uiFont(Font.BOLD, FS_VALUE));
        okBtn.setForeground(Color.WHITE);
        okBtn.setContentAreaFilled(false);
        okBtn.setBorderPainted(false);
        okBtn.setFocusPainted(false);
        okBtn.setMargin(new Insets(12, 36, 12, 36));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setRolloverEnabled(true);

        btnRow.add(cancelBtn);
        btnRow.add(okBtn);
        content.add(btnRow);

        confirmDialog.add(content, BorderLayout.CENTER);

        cancelBtn.addActionListener(e -> confirmDialog.dispose());

        okBtn.addActionListener(e -> {
            confirmDialog.dispose();
            loadPaperAndStartExam();
        });

        confirmDialog.getRootPane().registerKeyboardAction(
                e -> {}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        confirmDialog.setVisible(true);
    }

    /**
     * 后台加载试卷，成功后打开考试界面
     */
    private void loadPaperAndStartExam() {
        final JDialog loading = new JDialog(this, "正在加载", true);
        loading.setSize(360, 200);
        loading.setLocationRelativeTo(this);
        loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loading.setLayout(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PAGE_BG);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel msg = new JLabel("正在加载试卷，请稍候...", SwingConstants.CENTER);
        msg.setFont(uiFont(Font.PLAIN, FS_LABEL));
        msg.setForeground(TEXT_DARK);
        p.add(msg, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(false);
        bar.setBorderPainted(false);
        bar.setBackground(PAGE_BG);
        bar.setForeground(PRIMARY_BLUE);
        p.add(bar, BorderLayout.SOUTH);

        loading.add(p);

        new SwingWorker<ExamPaperVO, Void>() {
            @Override
            protected ExamPaperVO doInBackground() {
                ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();
                long planId = exam != null && exam.getPlanId() != null ? exam.getPlanId() : 0;
                long userId = 0;
                UserInfoVO user = LoginSession.get().getUserInfo();
                if (user != null && user.getId() != null) {
                    userId = user.getId();
                }
                return ApiUtil.getExamPaper(planId, userId);
            }

            @Override
            protected void done() {
                loading.dispose();
                try {
                    ExamPaperVO paper = get();
                    if (paper == null) {
                        JOptionPane.showMessageDialog(HomeFrame.this,
                                "请联系监考员获取试卷", "提示",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        ExamFrame examFrame = new ExamFrame(paper);
                        examFrame.setVisible(true);
                        HomeFrame.this.dispose();
                    });
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HomeFrame.this,
                                "请联系监考员获取试卷", "提示",
                                JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();

        loading.setVisible(true);
    }

    private void showWarningShortFilm(String videoUrl) {
        String fullUrl = videoUrl.startsWith("http") ? videoUrl : ApiUtil.getBaseUrl() + videoUrl;

        JDialog dialog = new JDialog(this, "观看警示短片", true);
        dialog.setSize(480, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBorder(new EmptyBorder(32, 40, 32, 40));
        content.setBackground(PAGE_BG);

        // JLabel iconLabel = new JLabel("▶", SwingConstants.CENTER);
        // iconLabel.setFont(new Font("Arial", Font.PLAIN, 48));
        // iconLabel.setForeground(PRIMARY_BLUE);
        // iconLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        // content.add(iconLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("正在准备播放警示短片...", SwingConstants.CENTER);
        titleLabel.setFont(uiFont(Font.BOLD, FS_TITLE));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        content.add(titleLabel, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel("视频将在系统播放器中自动播放", SwingConstants.CENTER);
        hintLabel.setFont(uiFont(Font.PLAIN, FS_HINT));
        hintLabel.setForeground(TEXT_GRAY);
        content.add(hintLabel, BorderLayout.SOUTH);

        dialog.add(content, BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                Path tmpFile;
                try {
                    URL url = new URL(fullUrl);
                    String ext = getFileExtension(fullUrl);
                    tmpFile = Files.createTempFile("ted_warn_", ext);
                    try (InputStream in = url.openStream();
                         OutputStream out = Files.newOutputStream(tmpFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("下载视频失败: " + ex.getMessage());
                    return null;
                }

                try {
                    String pathStr = tmpFile.toAbsolutePath().toString();
                    java.awt.Desktop.getDesktop().open(tmpFile.toFile());
                } catch (Exception ex) {
                    System.err.println("打开播放器失败: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                SwingUtilities.invokeLater(() -> showWatchedConfirmation());
            }
        }.execute();

        dialog.setVisible(true);
    }

    private static String getFileExtension(String url) {
        String path = url;
        int q = path.indexOf('?');
        if (q > 0) path = path.substring(0, q);
        int i = path.lastIndexOf('.');
        if (i >= 0) return path.substring(i);
        return ".mp4";
    }

    private void showWatchedConfirmation() {
        JDialog confirmDialog = new JDialog(this, "确认观看", true);
        confirmDialog.setSize(440, 320);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 40, 28, 40));
        content.setBackground(PAGE_BG);

        // JLabel iconLabel = new JLabel("▶", SwingConstants.CENTER);
        // iconLabel.setFont(new Font("Arial", Font.PLAIN, 36));
        // iconLabel.setForeground(PRIMARY_BLUE);
        // iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // iconLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        // content.add(iconLabel);

        JLabel titleLabel = new JLabel("请完整观看警示短片", SwingConstants.CENTER);
        titleLabel.setFont(uiFont(Font.BOLD, FS_TITLE));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));

        JLabel countdownLabel = new JLabel("强制观看时间：剩余 3:00", SwingConstants.CENTER);
        countdownLabel.setFont(uiFont(Font.PLAIN, FS_HINT));
        countdownLabel.setForeground(TEXT_GRAY);
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(countdownLabel);
        content.add(Box.createVerticalStrut(8));

        JLabel hintLabel = new JLabel("请在播放器中播放完毕，倒计时结束后方可确认", SwingConstants.CENTER);
        hintLabel.setFont(uiFont(Font.PLAIN, FS_HINT));
        hintLabel.setForeground(TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(hintLabel);
        content.add(Box.createVerticalStrut(24));

        JButton doneBtn = new JButton("已观看完毕") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor;
                if (!isEnabled()) {
                    bgColor = new Color(200, 200, 200);
                } else {
                    bgColor = PRIMARY_BLUE;
                    if (getModel().isRollover()) {
                        bgColor = PRIMARY_BLUE_HOVER;
                    }
                }
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        doneBtn.setFont(uiFont(Font.BOLD, FS_VALUE));
        doneBtn.setForeground(TEXT_DARK);
        doneBtn.setContentAreaFilled(false);
        doneBtn.setBorderPainted(false);
        doneBtn.setFocusPainted(false);
        doneBtn.setMargin(new Insets(14, 48, 14, 48));
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneBtn.setEnabled(false);

        final int totalSeconds = 180;
        final int[] remainingSeconds = {totalSeconds};
        Timer countdownTimer = new Timer(1000, null);
        countdownTimer.addActionListener(e -> {
            remainingSeconds[0]--;
            if (remainingSeconds[0] <= 0) {
                countdownTimer.stop();
                countdownLabel.setText("观看时间已到，请点击确认");
                countdownLabel.setForeground(new Color(34, 197, 94));
                doneBtn.setEnabled(true);
                doneBtn.setForeground(Color.WHITE);
                doneBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                doneBtn.repaint();
            } else {
                int min = remainingSeconds[0] / 60;
                int sec = remainingSeconds[0] % 60;
                countdownLabel.setText(String.format("强制观看时间：剩余 %d:%02d", min, sec));
            }
        });
        countdownTimer.start();

        doneBtn.addActionListener(e -> {
            countdownTimer.stop();
            shortFilmWatched = true;
            updateEnterButtonText();
            confirmDialog.dispose();
        });

        content.add(doneBtn);

        confirmDialog.add(content, BorderLayout.CENTER);
        confirmDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        // 屏蔽 ESC / 关闭按钮关闭
        confirmDialog.getRootPane().registerKeyboardAction(
                e -> {}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        confirmDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 禁止关闭，等待倒计时结束
            }
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                countdownTimer.stop();
            }
        });
        confirmDialog.setVisible(true);
    }

    private void showRules() {
        String rules = "考试须知\n\n" +
                "1. 考试时间为 90 分钟，请合理安排答题时间。\n" +
                "2. 考试期间请保持安静，遵守考场纪律。\n\n" +
                "注意事项\n\n" +
                "1. 考试系统出现异常请及时联系监考老师。\n\n" +
                "2. 【重要须知】考试作答时，严禁刷新页面、切换屏幕，\n" +
                "   违规将影响考试成绩。\n\n" +
                "3. 作答过程中请勿随意切换输入法或调整系统设置，\n" +
                "   防止页面卡顿。\n\n" +
                "4. 考试过程中若遇网络中断，请立即联系监考老师，\n" +
                "   切勿自行重启设备。\n\n" +
                "5. 禁止使用任何辅助软件、工具书或通讯设备，\n" +
                "   一经发现按作弊处理。\n\n" +
                "6. 交卷后请确认系统提示\"提交成功\"，再离开考场。";
        JTextArea ta = new JTextArea(rules);
        ta.setFont(uiFontPlain(FS_LABEL + 1));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setBackground(new Color(248, 250, 255));
        ta.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(480, 400));
        JOptionPane.showMessageDialog(this, scroll, "考试规则", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要退出登录吗？", "退出登录",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        LoginSession.get().logout();
        ApiUtil.clearAuthToken();

        dispose();
        LoginFrame login = new LoginFrame();
        login.setVisible(true);
    }

    // region 内部组件

    private static class SquareAvatarLabel extends JLabel {
        private static final int CORNER = 12;

        @Override
        protected void paintComponent(Graphics g) {
            if (getIcon() == null) {
                super.paintComponent(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER, CORNER);
            g2.setClip(clip);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(AVATAR_SIZE, AVATAR_SIZE);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    private class LightGrayBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(PAGE_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class MainShadowCardPanel extends JPanel {
        static final int RADIUS = 16;
        static final int SHADOW = 12;

        MainShadowCardPanel() {
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
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 6, w, h, RADIUS + 4, RADIUS + 4));
            g2.setColor(new Color(15, 23, 42, 6));
            g2.fill(new RoundRectangle2D.Float(x, y + 3, w, h, RADIUS + 2, RADIUS + 2));

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, RADIUS, RADIUS));

            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, RADIUS, RADIUS));
            g2.dispose();
        }
    }

    // endregion
}
