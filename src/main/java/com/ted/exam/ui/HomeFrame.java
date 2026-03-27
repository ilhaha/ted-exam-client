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
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import com.sun.jna.NativeLibrary;

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

        JPanel rightPad = new JPanel(new BorderLayout());
        rightPad.setOpaque(false);
        JPanel clockRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        clockRow.setOpaque(false);
        clockRow.add(clockLabel);
        rightPad.add(clockRow, BorderLayout.EAST);

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
        JPanel personalInfoGrid = new JPanel(new GridLayout(3, 1, fieldGap, fieldGap));
        personalInfoGrid.setOpaque(false);
        personalInfoGrid.add(makeFieldCard("考生姓名", nameValueLabel));
        personalInfoGrid.add(makeFieldCard("身份证号", idCardValueLabel));
        personalInfoGrid.add(makeFieldCard("准考证号", ticketValueLabel));

        // 考试信息：科目、考场、时间
        JPanel examInfoGrid = new JPanel(new GridLayout(3, 1, fieldGap, fieldGap));
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
                        + "5.禁止携带通讯工具、规定以外的电子用品或者与考试内容相关的资料进入座位，违反者按照违规违纪处理。\n\n"
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
            SwingUtilities.invokeLater(() -> showWarningShortFilm(warningShortFilm));
        } else {
            showEnterExamConfirm();
        }
    }

    /**
     * 弹出"确认开始考试"对话框，用户确认后加载试卷并进入考试
     */
    private void showEnterExamConfirm() {
        loadPaperAndStartExam();
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

    private static volatile boolean vlcInitialized = false;
    //本地测试
    // private static void initVlcPath() {
    //     if (vlcInitialized) return;
    //     synchronized (HomeFrame.class) {
    //         if (vlcInitialized) return;

    //         String vlcPath = "E:\\software\\VLC64";
    //         File vlcDir = new File(vlcPath);
    //         if (vlcDir.exists()) {
    //             File libvlc = new File(vlcDir, "libvlc.dll");
    //             if (libvlc.exists()) {
    //                 NativeLibrary.addSearchPath("libvlc", vlcPath);
    //                 vlcInitialized = true;
    //             }
    //         }

    //         String vlcHome = System.getenv("VLC_HOME");
    //         if (vlcHome != null) {
    //             File vlcHomeDir = new File(vlcHome);
    //             if (vlcHomeDir.exists() && new File(vlcHomeDir, "libvlc.dll").exists()) {
    //                 NativeLibrary.addSearchPath("libvlc", vlcHome);
    //                 vlcInitialized = true;
    //                 return;
    //             }
    //         }

    //     }
    // }

// 打包
private static void initVlcPath() {
    if (vlcInitialized) return;

    synchronized (HomeFrame.class) {
        if (vlcInitialized) return;

        // 获取程序当前目录
        String basePath = new File("").getAbsolutePath();

        // 拼接 vlc 目录（EXE 同级）
        File vlcDir = new File(basePath, "vlc");

        if (vlcDir.exists()) {
            File libvlc = new File(vlcDir, "libvlc.dll");
            if (libvlc.exists()) {

                // 推荐方式（你这个也可以）
                NativeLibrary.addSearchPath("libvlc", vlcDir.getAbsolutePath());

                // 更保险（建议一起加）
                System.setProperty("jna.library.path", vlcDir.getAbsolutePath());

                vlcInitialized = true;
                System.out.println("VLC加载成功：" + vlcDir.getAbsolutePath());
                return;
            }
        }

        System.err.println("未找到 VLC，本地视频播放不可用！");
    }
}

    private void showWarningShortFilm(String videoUrl) {
        initVlcPath();

        JDialog dialog = new JDialog(this, "观看警示短片", false);
        dialog.setSize(1100, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 播放期间禁止关闭
            }
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        content.setBackground(Color.BLACK);

        EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        content.add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        controlPanel.setBackground(new Color(30, 30, 30));

        final boolean[] videoEnded = {false};

        JLabel titleLabel = new JLabel("请完整观看警示短片，播放完毕后方可确认", SwingConstants.CENTER);
        titleLabel.setFont(uiFont(Font.PLAIN, FS_HINT));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setPreferredSize(new Dimension(400, 30));
        controlPanel.add(titleLabel);

        JButton closeBtn = new JButton("我已看完") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (videoEnded[0]) {
                    bg = new Color(37, 99, 235); // 蓝色
                    if (getModel().isRollover() && getModel().isEnabled()) {
                        bg = new Color(29, 78, 216); // 深蓝色 hover
                    }
                } else {
                    bg = new Color(60, 60, 60); // 灰色
                    if (getModel().isRollover() && getModel().isEnabled()) {
                        bg = new Color(80, 80, 80);
                    }
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(uiFont(Font.PLAIN, FS_LABEL));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setMargin(new Insets(8, 24, 8, 24));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setRolloverEnabled(true);
        closeBtn.setEnabled(false);
        controlPanel.add(closeBtn);

        content.add(controlPanel, BorderLayout.SOUTH);

        dialog.add(content, BorderLayout.CENTER);

        final AtomicBoolean playerReleased = new AtomicBoolean(false);

        Runnable releasePlayer = () -> {
            if (!playerReleased.compareAndSet(false, true)) {
                return;
            }
            try {
                MediaPlayer mp = mediaPlayerComponent.getMediaPlayer();
                if (mp != null) {
                    mp.stop();
                    mp.release();
                }
            } catch (Exception ignored) {
            }
        };

        mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                videoEnded[0] = true;
                SwingUtilities.invokeLater(() -> {
                    closeBtn.setEnabled(true);
                    closeBtn.requestFocusInWindow();
                });
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.err.println("VLC 播放错误: " + videoUrl);
                SwingUtilities.invokeLater(() -> {
                    titleLabel.setText("视频播放失败，请联系监考老师");
                    titleLabel.setForeground(new Color(255, 100, 100));
                    closeBtn.setEnabled(true);
                });
            }
        });

        closeBtn.addActionListener(e -> {
            releasePlayer.run();
            dialog.dispose();
            SwingUtilities.invokeLater(() -> {
                shortFilmWatched = true;
                updateEnterButtonText();
                loadPaperAndStartExam();
            });
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                    String mediaPath = resolveVideoPath(videoUrl);
                    if (mediaPath == null) {
                        titleLabel.setText("视频路径无效或文件不存在");
                        titleLabel.setForeground(new Color(255, 100, 100));
                        closeBtn.setEnabled(true);
                        return;
                    }
                    try {
                        mediaPlayerComponent.getMediaPlayer().playMedia(mediaPath);
                    } catch (Exception ex) {
                        System.err.println("播放视频失败: " + ex.getMessage());
                        titleLabel.setText("无法开始播放，请联系监考老师");
                        titleLabel.setForeground(new Color(255, 100, 100));
                        closeBtn.setEnabled(true);
                    }
                });
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 播放期间禁止通过窗口按钮关闭
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                releasePlayer.run();
            }
        });

        dialog.setVisible(true);
    }

    private String resolveVideoPath(String videoUrl) {
        if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
            return videoUrl;
        } else if (videoUrl.matches("^[a-zA-Z]:[/\\\\].*") || videoUrl.startsWith("/")) {
            File videoFile = new File(videoUrl);
            if (videoFile.exists()) {
                return videoFile.getAbsolutePath();
            }
            return null;
        } else {
            File videoFile = new File(videoUrl);
            if (videoFile.exists()) {
                return videoFile.getAbsolutePath();
            }
            return null;
        }
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
