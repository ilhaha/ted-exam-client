package com.ted.exam.ui;

import com.ted.exam.model.req.ExamRecordReq;
import com.ted.exam.model.resp.ExamCandidateInfoVO;
import com.ted.exam.model.resp.ExamPaperVO;
import com.ted.exam.model.resp.OptionVO;
import com.ted.exam.model.resp.QuestionBankWithOptionVO;
import com.ted.exam.model.resp.UserInfoVO;
import com.ted.exam.session.LoginSession;
import com.ted.exam.util.ApiUtil;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * 考试答题界面（布局参考特种设备作业人员考试客户端）
 */
public class ExamFrame extends JFrame {

    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color PRIMARY_BLUE_HOVER = new Color(29, 78, 216);
    private static final Color PAGE_BG = new Color(248, 250, 252);
    private static final Color CARD_INNER_BG = new Color(252, 253, 255);
    private static final Color STEM_BG = new Color(239, 246, 255);
    private static final Color STEM_BORDER = new Color(219, 234, 254);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_GRAY = new Color(71, 85, 105);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);
    private static final Color GRID_BORDER = new Color(226, 232, 240);
    private static final Color MARK_YELLOW = new Color(250, 204, 21);
    private static final Color TIMER_RED = new Color(220, 38, 38);
    private static final Color TIMER_CHIP_BG = new Color(254, 242, 242);
    private static final Color STAT_BLUE = new Color(37, 99, 235);
    private static final Color STAT_CHIP_BG = new Color(239, 246, 255);
    private static final Color OPTION_HOVER_BORDER = new Color(191, 219, 254);
    private static final Color SHADOW_SOFT = new Color(15, 23, 42, 14);
    private static final Color SHADOW_CARD = new Color(15, 23, 42, 10);

    private static final float FS_PAGE_TITLE = 18f;
    private static final float FS_CARD_TITLE = 18f;
    private static final float FS_BODY = 16f;
    private static final float FS_BTN = 14f;
    private static final float FS_SMALL = 12f;

    private static Font uiFont(int style, float sizePx) {
        Font base = new Font("Microsoft YaHei UI", style, 14);
        return base.deriveFont(sizePx);
    }

    private static Font uiFontPlain(float sizePx) {
        return uiFont(Font.PLAIN, sizePx);
    }

    private final ExamPaperVO paper;
    private final List<QuestionBankWithOptionVO> questions;
    private final Map<Long, Set<Long>> answers = new LinkedHashMap<>();
    /** 已标记的题目序号（0-based） */
    private final Set<Integer> markedIndices = new HashSet<>();

    private int currentIndex = 0;
    private JPanel questionBody;
    private JScrollPane questionScroll;
    private JLabel timerLabel;
    private JLabel answeredStatLabel;
    private JLabel unansweredStatLabel;
    private Timer examTimer;
    private int remainingSeconds;

    private JButton prevBtn;
    private JButton nextBtn;
    private JLabel cardTitleLabel;
    private JPanel navGrid;
    private JButton markBtn;
    private JButton submitBtn;
    private JLabel photoLabel;
    private int gridCols = 10;
    private int navGap = 5;
    private Dimension navCellSize = new Dimension(26, 26);
    private static final int PHOTO_SIZE = 120;

    public ExamFrame(ExamPaperVO paper) {
        this.paper = paper;
        this.questions = paper.getQuestions() != null ? paper.getQuestions() : new ArrayList<>();

        ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();
        int duration = exam != null && exam.getExamDuration() != null ? exam.getExamDuration() : 90;
        remainingSeconds = duration * 60;

        initFrame();
        buildUI();
        rebuildNavGrid();
        updateHeaderStats();
        loadQuestion(0);
        startTimer();
    }

    private void initFrame() {
        setTitle("在线考试");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);

        root.add(buildExamTitleBar(), BorderLayout.NORTH);
        root.add(buildHeaderBar(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);

        add(root);
    }

    private JComponent buildExamTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY_BLUE);
        bar.setPreferredSize(new Dimension(0, 44));

        JLabel title = new JLabel("在线考试", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.CENTER);

        return bar;
    }

    /** 顶栏：考试名称 + 剩余时间 / 已答 / 未答 */
    private JComponent buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                new EmptyBorder(16, 32, 16, 32)));

        ExamCandidateInfoVO exam = LoginSession.get().getExamInfo();
        String examTitle = exam != null && exam.getPlanName() != null && !exam.getPlanName().isEmpty()
                ? exam.getPlanName()
                : "特种设备作业人员考试";

        JLabel title = new JLabel(examTitle, SwingConstants.LEADING);
        title.setFont(uiFont(Font.BOLD, FS_PAGE_TITLE));
        title.setForeground(TEXT_DARK);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        timerLabel = new JLabel();
        timerLabel.setFont(uiFont(Font.BOLD, FS_BODY - 1f));
        timerLabel.setForeground(TIMER_RED);
        timerLabel.setBorder(new EmptyBorder(6, 14, 6, 14));

        answeredStatLabel = new JLabel();
        answeredStatLabel.setFont(uiFont(Font.BOLD, FS_BODY - 1f));
        answeredStatLabel.setForeground(STAT_BLUE);
        answeredStatLabel.setBorder(new EmptyBorder(6, 14, 6, 14));

        unansweredStatLabel = new JLabel();
        unansweredStatLabel.setFont(uiFont(Font.BOLD, FS_BODY - 1f));
        unansweredStatLabel.setForeground(STAT_BLUE);
        unansweredStatLabel.setBorder(new EmptyBorder(6, 14, 6, 14));

        right.add(wrapHeaderChip(timerLabel, TIMER_CHIP_BG));
        right.add(headerSep());
        right.add(wrapHeaderChip(answeredStatLabel, STAT_CHIP_BG));
        right.add(headerSep());
        right.add(wrapHeaderChip(unansweredStatLabel, STAT_CHIP_BG));
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private static JComponent headerSep() {
        JLabel s = new JLabel();
        s.setPreferredSize(new Dimension(1, 24));
        s.setMaximumSize(new Dimension(1, 24));
        s.setOpaque(true);
        s.setBackground(BORDER_LIGHT);
        return s;
    }

    /** 顶栏信息胶囊底 */
    private static JComponent wrapHeaderChip(JLabel inner, Color chipBg) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(chipBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_LIGHT);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildMainArea() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(8, 12, 12, 12));

        JComponent infoSidebar = buildInfoSidebar();
        JComponent left = buildQuestionCard();
        JComponent right = buildAnswerSidebar();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.65); // 左 65% / 右 35%
        split.setBackground(PAGE_BG);
        // 确保首次显示时按 65/35 落位（否则默认 divider 可能偏移）
        split.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && split.isShowing()) {
                int w = split.getWidth();
                if (w > 0) split.setDividerLocation((int) (w * 0.65));
            }
        });

        wrap.add(infoSidebar, BorderLayout.WEST);
        wrap.add(split, BorderLayout.CENTER);
        return wrap;
    }

    /** 左侧个人信息栏 */
    private JComponent buildInfoSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(CARD_INNER_BG);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 14, 16, 14)));
        side.setPreferredSize(new Dimension(240, 0));
        side.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));

        // 整块置顶：避免 CENTER + BoxLayout 纵向拉伸把信息挤到底部
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("考生信息", SwingConstants.CENTER);
        title.setFont(uiFont(Font.BOLD, FS_BTN));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        column.add(title);

        JPanel photoPanel = buildPhotoPanel();
        photoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(photoPanel);
        column.add(Box.createVerticalStrut(14));

        LoginSession s = LoginSession.get();
        UserInfoVO user = s.getUserInfo();

        addInfoRowToColumn(column, "考生姓名", nvl(user != null ? user.getNickname() : null, "—"));
        column.add(Box.createVerticalStrut(10));
        addInfoRowToColumn(column, "身份证号", nvl(user != null ? user.getUsername() : null, "—"));
        column.add(Box.createVerticalStrut(10));
        addInfoRowToColumn(column, "准考证号", nvl(s.getExamNumber(), "—"));
        column.add(Box.createVerticalStrut(10));
        addInfoRowToColumn(column, "考场名称", nvl(s.getClassroomName(), "—"));

        side.add(column, BorderLayout.NORTH);
        return side;
    }

    private void addInfoRowToColumn(JPanel column, String label, String value) {
        JPanel row = infoRow(label, value);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(row);
    }

    private static String nvl(String s, String def) {
        return s != null && !s.isEmpty() ? s : def;
    }

    /** 照片区域：圆角头像占位 */
    private JPanel buildPhotoPanel() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel photoArea = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int r = 10;
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, w, h, r, r);
                g2.setColor(BORDER_LIGHT);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(PHOTO_SIZE, PHOTO_SIZE);
            }
        };
        photoArea.setMaximumSize(new Dimension(PHOTO_SIZE, PHOTO_SIZE));
        photoArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        photoLabel = new SquarePhotoLabel();
        photoLabel.setFont(uiFont(Font.BOLD, 36f));
        photoLabel.setForeground(PRIMARY_BLUE);
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);
        photoLabel.setText(getPhotoInitials());
        photoArea.add(photoLabel, BorderLayout.CENTER);

        JPanel photoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        photoWrap.setOpaque(false);
        photoWrap.add(photoArea);

        wrap.add(photoWrap);

        // 异步加载照片
        String photoUrl = LoginSession.get().getFacePhoto();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            final String finalUrl = photoUrl.startsWith("http")
                    ? photoUrl
                    : ApiUtil.getBaseUrl() + photoUrl;
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() {
                    try {
                        URL url = new URL(finalUrl);
                        ImageIcon icon = new ImageIcon(url);
                        Image scaled = icon.getImage().getScaledInstance(
                                PHOTO_SIZE, PHOTO_SIZE, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    } catch (Exception ex) {
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon icon = get();
                        if (icon != null && photoLabel != null) {
                            photoLabel.setIcon(icon);
                            photoLabel.setText(null);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }.execute();
        }

        // JLabel hint = new JLabel("考生照片", SwingConstants.CENTER);
        // hint.setFont(uiFontPlain(FS_SMALL));
        // hint.setForeground(TEXT_MUTED);
        // hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        // hint.setMaximumSize(new Dimension(PHOTO_SIZE, 20));
        // wrap.add(hint);
        return wrap;
    }

    /** 获取姓名缩写用于头像占位 */
    private String getPhotoInitials() {
        UserInfoVO user = LoginSession.get().getUserInfo();
        String name = user != null ? user.getNickname() : null;
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    /** 带圆角裁切的图片标签 */
    private static class SquarePhotoLabel extends JLabel {
        private static final int CORNER = 10;

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
            return new Dimension(PHOTO_SIZE, PHOTO_SIZE);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

    /** 单行信息标签 */
    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = new JLabel(label);
        l.setFont(uiFontPlain(FS_SMALL));
        l.setForeground(TEXT_MUTED);
        l.setPreferredSize(new Dimension(56, 20));

        JLabel v = new JLabel(value);
        v.setFont(uiFont(Font.PLAIN, FS_SMALL));
        v.setForeground(TEXT_DARK);
        v.setBorder(new EmptyBorder(0, 0, 0, 0));

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
    }

    /** 中间白色答题卡（圆角阴影） */
    private JComponent buildQuestionCard() {
        ShadowWhitePanel card = new ShadowWhitePanel();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(16, 20, 18, 20));

        card.add(buildCardNavRow(), BorderLayout.NORTH);

        questionBody = new ExamQuestionBodyPanel();
        questionBody.setLayout(new GridBagLayout());
        questionBody.setOpaque(false);

        questionScroll = new JScrollPane(questionBody);
        questionScroll.setBorder(null);
        questionScroll.getViewport().setBackground(CARD_INNER_BG);
        questionScroll.getViewport().setOpaque(true);
        questionScroll.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        questionScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        questionScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        card.add(questionScroll, BorderLayout.CENTER);

        return card;
    }

    /** 上一题 | 第 N 题 题型 | 下一题 */
    private JComponent buildCardNavRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 14, 0));

        prevBtn = createOutlineNavButton("上一题", true);
        prevBtn.addActionListener(e -> {
            if (currentIndex > 0) {
                currentIndex--;
                loadQuestion(currentIndex);
                refreshExamState();
            }
        });

        nextBtn = createOutlineNavButton("下一题", false);
        nextBtn.addActionListener(e -> {
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                loadQuestion(currentIndex);
                refreshExamState();
            }
        });

        cardTitleLabel = new JLabel("第1题  单选题", SwingConstants.CENTER);
        cardTitleLabel.setFont(uiFont(Font.BOLD, FS_CARD_TITLE));
        cardTitleLabel.setForeground(PRIMARY_BLUE);

        row.add(prevBtn, BorderLayout.WEST);
        row.add(cardTitleLabel, BorderLayout.CENTER);
        row.add(nextBtn, BorderLayout.EAST);
        return row;
    }

    private JButton createOutlineNavButton(String text, boolean isPrev) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = 10;
                Color border;
                Color fg;
                Color bg = new Color(252, 252, 253);
                if (!isEnabled()) {
                    border = new Color(226, 232, 240);
                    fg = TEXT_MUTED;
                } else if (getModel().isRollover()) {
                    border = PRIMARY_BLUE;
                    fg = PRIMARY_BLUE;
                    bg = new Color(239, 246, 255);
                } else {
                    border = BORDER_LIGHT;
                    fg = TEXT_GRAY;
                }
                g2.setColor(bg);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, r, r);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, r, r);
                g2.dispose();
                setForeground(fg);
                super.paintComponent(g);
            }
        };
        b.setFont(uiFont(Font.BOLD, FS_BTN));
        b.setForeground(TEXT_GRAY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(10, 22, 10, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setRolloverEnabled(true);
        return b;
    }

    /** 右侧答题卡（内容自顶向下紧凑排列，避免大块留白） */
    private JComponent buildAnswerSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(CARD_INNER_BG);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(14, 14, 14, 14)));
        side.setPreferredSize(new Dimension(340, 0));
        side.setMaximumSize(new Dimension(360, Integer.MAX_VALUE));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        // 答题卡标题
        JLabel answerCardTitle = new JLabel("答题卡", SwingConstants.CENTER);
        answerCardTitle.setFont(uiFont(Font.BOLD, FS_BTN));
        answerCardTitle.setForeground(TEXT_DARK);
        answerCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerCardTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        stack.add(answerCardTitle);

        JComponent legend = buildLegendPanel();
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(legend);
        stack.add(Box.createVerticalStrut(10));

        int n = questions.size();
        gridCols = Math.min(10, Math.max(1, n));
        navGrid = new JPanel(new GridLayout(0, gridCols, navGap, navGap));
        navGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        navGrid.setOpaque(false);
        JPanel gridBox = new JPanel(new BorderLayout());
        gridBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        gridBox.setOpaque(false);
        gridBox.add(navGrid, BorderLayout.CENTER);
        updateNavGridPreferredSize();
        gridBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, navGrid.getPreferredSize().height));
        stack.add(gridBox);
        stack.add(Box.createVerticalStrut(16));

        // 标记按钮
        markBtn = createModernButton(
                "标记 / 取消",
                new Color(254, 228, 228),
                new Color(254, 200, 200),
                TIMER_RED,
                e -> {
                    if (markedIndices.contains(currentIndex)) {
                        markedIndices.remove(currentIndex);
                    } else {
                        markedIndices.add(currentIndex);
                    }
                    refreshExamState();
                }
        );
        markBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        stack.add(markBtn);
        stack.add(Box.createVerticalStrut(10));

        // 提交按钮
        submitBtn = createModernButton(
                "提交试卷",
                new Color(37, 99, 235),
                new Color(29, 78, 216),
                Color.WHITE,
                e -> doSubmit()
        );
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        stack.add(submitBtn);

        side.add(stack, BorderLayout.NORTH);
        return side;
    }

    private JButton createModernButton(String text, Color baseColor, Color hoverColor, Color textColor, ActionListener action) {
        JButton btn = new JButton(text) {
            private boolean hovering = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) {
                            hovering = true;
                            repaint();
                        }
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color actualBaseColor = baseColor;
                Color actualHoverColor = hoverColor;
                Color actualTextColor = textColor;

                if (!isEnabled()) {
                    actualBaseColor = new Color(189, 195, 199);
                    actualHoverColor = new Color(189, 195, 199);
                    actualTextColor = new Color(127, 140, 141);
                    hovering = false;
                }

                GradientPaint gp = new GradientPaint(
                        0, 0,
                        hovering ? actualHoverColor.brighter() : actualBaseColor,
                        0, h,
                        hovering ? actualHoverColor : actualBaseColor.darker()
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                g2.dispose();
                setForeground(actualTextColor);
                super.paintComponent(g);
            }
        };
        btn.setFont(uiFont(Font.BOLD, FS_CARD_TITLE));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        if (action != null) {
            btn.addActionListener(action);
        }
        return btn;
    }

    private JComponent buildLegendPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(0, 0, 5, 0));
        p.add(legendRow(squareSampleOutline(PRIMARY_BLUE), "当前题"));
        p.add(legendRow(squareSample(PRIMARY_BLUE, true), "已答题"));
        p.add(legendRow(squareSample(MARK_YELLOW, true), "标记"));
        p.add(legendRow(squareSampleOutline(GRID_BORDER), "未答题"));
        return p;
    }

    private JComponent legendRow(JComponent sample, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel t = new JLabel(text);
        t.setFont(uiFontPlain(FS_SMALL));
        t.setForeground(TEXT_GRAY);
        row.add(sample);
        row.add(t);
        return row;
    }

    private JComponent squareSample(Color fill, boolean solid) {
        JLabel l = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (solid) {
                    g2.setColor(fill);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }
        };
        l.setOpaque(false);
        return l;
    }

    private JComponent squareSampleOutline(Color c) {
        JLabel l = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 4, 4);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }
        };
        l.setOpaque(false);
        return l;
    }

    private void rebuildNavGrid() {
        if (navGrid == null) return;
        navGrid.removeAll();
        for (int i = 0; i < questions.size(); i++) {
            final int navIndex = i;
            final QuestionBankWithOptionVO q = questions.get(i);
            JButton btn = new JButton(String.valueOf(i + 1)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    int r = 4;
                    boolean current = navIndex == currentIndex;
                    boolean answered = isQuestionAnswered(q);
                    boolean marked = markedIndices.contains(navIndex);

                    if (current) {
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
                        g2.setColor(PRIMARY_BLUE);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
                    } else if (marked) {
                        g2.setColor(MARK_YELLOW);
                        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
                        g2.setColor(GRID_BORDER);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(0, 0, w - 2, h - 2, r, r);
                    } else if (answered) {
                        g2.setColor(PRIMARY_BLUE);
                        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
                        g2.setColor(GRID_BORDER);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(0, 0, w - 2, h - 2, r, r);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(uiFontPlain(FS_SMALL));
            if (navIndex == currentIndex) {
                btn.setForeground(PRIMARY_BLUE);
            } else if (isQuestionAnswered(q)) {
                btn.setForeground(Color.WHITE);
            } else {
                btn.setForeground(TEXT_DARK);
            }
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(3, 1, 3, 1));
            btn.setPreferredSize(navCellSize);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final int idx = i;
            btn.addActionListener(e -> {
                currentIndex = idx;
                loadQuestion(idx);
                refreshExamState();
            });
            navGrid.add(btn);
        }
        navGrid.revalidate();
        navGrid.repaint();
        updateNavGridPreferredSize();
    }

    private void updateNavGridPreferredSize() {
        if (navGrid == null) return;
        int n = questions.size();
        int cols = gridCols <= 0 ? 10 : gridCols;
        int rows = (int) Math.ceil(n / (double) cols);
        if (rows <= 0) rows = 1;

        int w = cols * navCellSize.width + Math.max(0, cols - 1) * navGap;
        int h = rows * navCellSize.height + Math.max(0, rows - 1) * navGap;
        Dimension d = new Dimension(w, h);
        navGrid.setPreferredSize(d);
        navGrid.setMaximumSize(d);
    }

    private void loadQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;
        QuestionBankWithOptionVO q = questions.get(index);

        if (cardTitleLabel != null) {
            cardTitleLabel.setText("第" + (index + 1) + "题  " + q.getQuestionTypeName());
        }

        questionBody.removeAll();

        JTextArea questionText = new JTextArea(q.getQuestion() != null ? q.getQuestion() : "");
        questionText.setFont(uiFont(Font.BOLD, FS_BODY));
        questionText.setForeground(TEXT_DARK);
        questionText.setLineWrap(true);
        questionText.setWrapStyleWord(true);
        questionText.setEditable(false);
        questionText.setOpaque(false);
        questionText.setBorder(new EmptyBorder(0, 0, 0, 0));
        questionText.setRows(0);
        questionText.setColumns(1);
        // 限制题干 JTextArea 高度，避免在滚动视口中纵向被撑满
        int bodyW = questionBody.getWidth();
        if (bodyW <= 0 && questionScroll != null) {
            bodyW = questionScroll.getViewport().getWidth();
        }
        if (bodyW <= 0) {
            bodyW = 720;
        }
        bodyW = Math.max(bodyW - 8, 200);
        questionText.setSize(bodyW, 1);
        int qh = questionText.getPreferredSize().height;
        questionText.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(qh, 24)));

        ModernStemPanel stem = new ModernStemPanel();
        stem.setLayout(new BorderLayout());
        stem.setBorder(new EmptyBorder(16, 22, 16, 20));
        stem.add(questionText, BorderLayout.CENTER);
        stem.setMaximumSize(new Dimension(Integer.MAX_VALUE, qh + 36));

        JPanel questionBlock = new JPanel(new BorderLayout());
        questionBlock.setOpaque(false);
        questionBlock.add(stem, BorderLayout.NORTH);

        GridBagConstraints qbc = new GridBagConstraints();
        qbc.gridx = 0;
        qbc.gridy = 0;
        qbc.weightx = 1;
        qbc.weighty = 0;
        qbc.fill = GridBagConstraints.HORIZONTAL;
        qbc.anchor = GridBagConstraints.NORTHWEST;
        qbc.insets = new Insets(0, 0, 0, 0);
        questionBody.add(questionBlock, qbc);

        List<OptionVO> options = q.getOptions();
        if (options != null && !options.isEmpty()) {
            int type = q.getQuestionType() != null ? q.getQuestionType() : 1;
            Set<Long> selected = answers.getOrDefault(q.getId(), new HashSet<>());
            final int optHGap = 12;
            int contentW = Math.max(bodyW, 320);
            int cellW = Math.max((contentW - optHGap) / 2, 160);

            int pairCount = (int) Math.ceil(options.size() / 2.0);
            JPanel optionsGrid = new JPanel(new GridLayout(pairCount, 2, optHGap, 12));
            optionsGrid.setOpaque(false);

            for (int oi = 0; oi < options.size(); oi++) {
                OptionVO opt = options.get(oi);
                String letter = optionLetter(opt, oi);
                OptionRowPanel row = new OptionRowPanel(opt, letter, selected, type == 2, q.getId(), cellW);

                JPanel cell = new JPanel(new BorderLayout());
                cell.setOpaque(false);
                cell.add(row, BorderLayout.CENTER);
                optionsGrid.add(cell);
            }
            if (options.size() % 2 != 0) {
                JPanel emptyCell = new JPanel();
                emptyCell.setOpaque(false);
                optionsGrid.add(emptyCell);
            }

            GridBagConstraints obc = new GridBagConstraints();
            obc.gridx = 0;
            obc.gridy = 1;
            obc.weightx = 1;
            obc.weighty = 0;
            obc.fill = GridBagConstraints.HORIZONTAL;
            obc.anchor = GridBagConstraints.NORTHWEST;
            obc.insets = new Insets(8, 0, 0, 0);
            questionBody.add(optionsGrid, obc);
        }

        if (prevBtn != null) {
            prevBtn.setEnabled(index > 0);
        }
        if (nextBtn != null) {
            nextBtn.setEnabled(index < questions.size() - 1);
        }

        questionBody.revalidate();
        questionBody.repaint();
    }

    private static String optionLetter(OptionVO opt, int index) {
        String k = opt.getOptionKey();
        if (k != null && !k.trim().isEmpty()) {
            return k.trim();
        }
        if (index >= 0 && index < 26) {
            return String.valueOf((char) ('A' + index));
        }
        return String.valueOf(index + 1);
    }

    private boolean isQuestionAnswered(QuestionBankWithOptionVO q) {
        Set<Long> sel = answers.get(q.getId());
        return sel != null && !sel.isEmpty();
    }

    private void refreshExamState() {
        rebuildNavGrid();
        updateHeaderStats();
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (submitBtn == null) return;

        int total = questions.size();
        int answered = 0;
        for (QuestionBankWithOptionVO q : questions) {
            if (isQuestionAnswered(q)) answered++;
        }

        boolean allAnswered = answered == total;
        boolean hasMarked = !markedIndices.isEmpty();

        submitBtn.setEnabled(allAnswered && !hasMarked);
        submitBtn.repaint();
    }

    private void updateHeaderStats() {
        int total = questions.size();
        int answered = 0;
        for (QuestionBankWithOptionVO q : questions) {
            if (isQuestionAnswered(q)) answered++;
        }
        if (answeredStatLabel != null) {
            answeredStatLabel.setText("已答: " + answered + " 题");
        }
        if (unansweredStatLabel != null) {
            unansweredStatLabel.setText("未答: " + (total - answered) + " 题");
        }
    }

    private void startTimer() {
        updateTimerDisplay();
        examTimer = new Timer(1000, e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                examTimer.stop();
                autoSubmitAfterDelay();
            }
        });
        examTimer.start();
    }

    private void autoSubmitAfterDelay() {
        final JDialog dialog = new JDialog(this, "时间到", true);
        dialog.setLayout(new BorderLayout());
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JLabel msg = new JLabel("考试时间已到，3 秒后系统将自动交卷！", SwingConstants.CENTER);
        msg.setFont(uiFont(Font.BOLD, 16f));
        msg.setBorder(new EmptyBorder(30, 40, 10, 40));

        JLabel countLabel = new JLabel("3", SwingConstants.CENTER);
        countLabel.setFont(uiFont(Font.BOLD, 48f));
        countLabel.setForeground(TIMER_RED);
        countLabel.setBorder(new EmptyBorder(10, 40, 30, 40));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(msg, BorderLayout.NORTH);
        center.add(countLabel, BorderLayout.CENTER);
        dialog.add(center, BorderLayout.CENTER);

        dialog.pack();
        dialog.setLocationRelativeTo(this);

        final int[] remaining = {3};
        ActionListener countdownAction = e -> {
            remaining[0]--;
            countLabel.setText(String.valueOf(remaining[0]));
            if (remaining[0] <= 0) {
                ((Timer) e.getSource()).stop();
                dialog.dispose();
                submitExam();
            }
        };
        Timer countdown = new Timer(1000, countdownAction);
        countdown.start();

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                countdown.stop();
                submitExam();
            }
        });

        dialog.setVisible(true);
    }

    private void updateTimerDisplay() {
        if (timerLabel == null) return;
        int hours = remainingSeconds / 3600;
        int min = (remainingSeconds % 3600) / 60;
        int sec = remainingSeconds % 60;
        // 统一时:分:秒，避免 90 分钟卷面显示成「89:48」这类易被误读的 mm:ss
        timerLabel.setText(String.format("剩余时间: %02d:%02d:%02d", hours, min, sec));
        timerLabel.setForeground(TIMER_RED);
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "考试未提交，确定要退出吗？退出后成绩将无效！",
                "确认退出", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            examTimer.stop();
            dispose();
        }
    }

    private void doSubmit() {
        int total = questions.size();
        int answered = 0;
        for (QuestionBankWithOptionVO q : questions) {
            if (isQuestionAnswered(q)) answered++;
        }
        boolean allAnswered = answered == total;
        boolean hasMarked = !markedIndices.isEmpty();

        if (!allAnswered) {
            int unanswered = total - answered;
            JOptionPane.showMessageDialog(this,
                    "还有 " + unanswered + " 道题未作答，请先完成所有题目！",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hasMarked) {
            JOptionPane.showMessageDialog(this,
                    "还有 " + markedIndices.size() + " 道题被标记，请先取消所有标记！",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "您确定要交卷吗？交卷后无法修改答案。",
                "确认交卷", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            submitExam();
        }
    }

    private void submitExam() {
        examTimer.stop();

        int finalScore = calculateSubmitScore();
        int totalScore = questions.size();

        LoginSession session = LoginSession.get();
        ExamCandidateInfoVO examInfo = session.getExamInfo();
        UserInfoVO userInfo = session.getUserInfo();

        Long planId = examInfo != null ? examInfo.getPlanId() : null;
        Long candidateId = userInfo != null ? userInfo.getId() : null;

        String infoHint = (planId == null || candidateId == null)
                ? "缺少考试计划或考生信息，成绩仅在本地计算，未上传到服务器。"
                : null;
        String apiHint = null;
        if (infoHint == null) {
            // 构建带用户答案的试卷数据（每题内部包含用户答案）
            List<Map<String, Object>> questionsWithAnswers = new ArrayList<>();
            for (QuestionBankWithOptionVO q : questions) {
                Map<String, Object> qData = new HashMap<>();
                qData.put("id", q.getId());
                qData.put("question", q.getQuestion());
                qData.put("questionType", q.getQuestionType());
                qData.put("questionTypeName", q.getQuestionTypeName());
                qData.put("options", q.getOptions());
                qData.put("attachment", q.getAttachment());
                qData.put("knowledgeTypeId", q.getKnowledgeTypeId());
                qData.put("knowledgeTypeName", q.getKnowledgeTypeName());
                qData.put("knowledgeTypeTopicNumber", q.getKnowledgeTypeTopicNumber());

                Set<Long> selected = answers.get(q.getId());
                if (selected != null && !selected.isEmpty()) {
                    qData.put("userAnswer", new ArrayList<>(selected));
                } else {
                    qData.put("userAnswer", new ArrayList<>());
                }
                questionsWithAnswers.add(qData);
            }

            Map<String, Object> paperData = new HashMap<>();
            paperData.put("questions", questionsWithAnswers);
            String examPaperJson = new Gson().toJson(paperData);

            ExamRecordReq req = new ExamRecordReq();
            req.setPlanId(planId);
            req.setCandidateId(candidateId);
            req.setRegistrationProgress(3);
            req.setExamScores(finalScore);
            req.setReviewStatus(0);
            req.setExamPaper(examPaperJson);
            req.setViolationType(0);
            req.setViolationScreenshots(new ArrayList<>());

            try {
                boolean success = ApiUtil.submitExamRecord(req);
                if (!success) {
                    apiHint = "服务器未确认保存成功，请向监考员核实提交状态。";
                }
            } catch (Exception e) {
                apiHint = "提交考试记录失败：" + e.getMessage();
            }
        }

        final String hint = infoHint != null ? infoHint : (apiHint != null ? apiHint : null);
        dispose();
        SwingUtilities.invokeLater(() -> {
            ExamEndFrame endFrame = new ExamEndFrame(finalScore, totalScore, hint);
            endFrame.setVisible(true);
        });
    }

    private int calculateSubmitScore() {
        int correctCount = 0;
        for (QuestionBankWithOptionVO q : questions) {
            Set<Long> selected = answers.get(q.getId());
            if (selected == null || selected.isEmpty()) continue;

            List<OptionVO> options = q.getOptions();
            if (options == null) continue;

            int qType = q.getQuestionType() != null ? q.getQuestionType() : 0;

            if (qType == 0 || qType == 1) {
                // 单选题或判断题
                for (OptionVO opt : options) {
                    if (opt.getId() != null && Boolean.TRUE.equals(opt.getIsCorrectAnswer())
                            && selected.contains(opt.getId())) {
                        correctCount++;
                        break;
                    }
                }
            } else if (qType == 2) {
                // 多选题
                List<OptionVO> correctOptions = new ArrayList<>();
                List<OptionVO> selectedOptions = new ArrayList<>();
                for (OptionVO opt : options) {
                    if (opt.getId() != null && Boolean.TRUE.equals(opt.getIsCorrectAnswer())) {
                        correctOptions.add(opt);
                    }
                    if (opt.getId() != null && selected.contains(opt.getId())) {
                        selectedOptions.add(opt);
                    }
                }
                if (selectedOptions.size() == correctOptions.size()) {
                    boolean allCorrect = true;
                    for (OptionVO opt : selectedOptions) {
                        if (!Boolean.TRUE.equals(opt.getIsCorrectAnswer())) {
                            allCorrect = false;
                            break;
                        }
                    }
                    if (allCorrect) {
                        correctCount++;
                    }
                }
            }
        }
        return correctCount;
    }

    // region 内部组件

    /** 题干浅底卡片 + 左侧主色条 */
    private static final class ModernStemPanel extends JPanel {
        ModernStemPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            if (w < 4 || h < 4) {
                g2.dispose();
                return;
            }
            g2.setColor(STEM_BG);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            int barH = Math.max(h - 28, 24);
            int barY = (h - barH) / 2;
            g2.setColor(PRIMARY_BLUE);
            g2.fillRoundRect(6, barY, 4, barH, 3, 3);
            g2.setColor(STEM_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w, h, 14, 14);
            g2.dispose();
        }
    }

    /**
     * 题干+选项区域：实现 Scrollable，高度不随视口拉伸，避免下方大块空白；
     * 宽度跟随视口，便于 JTextArea 换行。
     */
    private static final class ExamQuestionBodyPanel extends JPanel implements Scrollable {
        ExamQuestionBodyPanel() {
            super();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? 16 : 10;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL
                    ? Math.max(visibleRect.height - 24, 16)
                    : Math.max(visibleRect.width - 24, 10);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** 带浅阴影的白底卡片 */
    private static class ShadowWhitePanel extends JPanel {
        private static final int R = 16;
        private static final int SH = 8;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = SH;
            int y = SH;
            int w = getWidth() - SH * 2;
            int h = getHeight() - SH * 2;
            g2.setColor(SHADOW_SOFT);
            g2.fill(new RoundRectangle2D.Float(x + 2, y + 10, w, h, R + 4, R + 4));
            g2.setColor(SHADOW_CARD);
            g2.fill(new RoundRectangle2D.Float(x + 1, y + 5, w, h, R + 2, R + 2));
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(x, y, w, h, R, R));
            g2.setColor(new Color(241, 245, 249));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, R, R));
            g2.dispose();
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    /** 选项横条：圆角边框 + 左侧字母圆 + 文案 */
    private class OptionRowPanel extends JPanel {
        private final OptionVO option;
        private final String letter;
        private final boolean isMulti;
        private final Long questionId;
        private boolean selectedState;
        private boolean hoverState;
        private final int rowMaxWidth;

        OptionRowPanel(OptionVO option, String letter, Set<Long> selected, boolean isMulti, Long questionId, int rowMaxWidth) {
            this.option = option;
            this.letter = letter;
            this.isMulti = isMulti;
            this.questionId = questionId;
            this.rowMaxWidth = rowMaxWidth;
            this.selectedState = option.getId() != null && selected.contains(option.getId());

            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            left.setOpaque(false);
            JLabel circle = new JLabel(letter) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int d = Math.min(getWidth(), getHeight());
                    Color fill = selectedState ? PRIMARY_BLUE : new Color(241, 245, 249);
                    if (!selectedState && OptionRowPanel.this.hoverState) {
                        fill = new Color(226, 232, 240);
                    }
                    g2.setColor(fill);
                    g2.fillOval(0, 0, d - 1, d - 1);
                    g2.setColor(selectedState ? new Color(147, 197, 253) : new Color(203, 213, 225));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawOval(0, 0, d - 2, d - 2);
                    g2.dispose();
                    super.paintComponent(g);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(30, 30);
                }
            };
            circle.setFont(uiFont(Font.BOLD, FS_BODY));
            circle.setForeground(selectedState ? Color.WHITE : TEXT_DARK);
            circle.setHorizontalAlignment(SwingConstants.CENTER);
            circle.setVerticalAlignment(SwingConstants.CENTER);
            left.add(circle);

            String val = option.getOptionValue();
            if (val == null) val = "";
            JTextArea label = new JTextArea(val);
            label.setFont(uiFontPlain(FS_BODY));
            label.setForeground(TEXT_DARK);
            label.setLineWrap(true);
            label.setWrapStyleWord(true);
            label.setEditable(false);
            label.setOpaque(false);
            label.setBorder(null);
            label.setRows(0);
            label.setColumns(1);
            int optW = Math.max(rowMaxWidth - 56, 140);
            label.setSize(optW, 1);
            int lh = label.getPreferredSize().height;
            label.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(lh, 22)));

            JPanel textWrap = new JPanel(new BorderLayout());
            textWrap.setOpaque(false);
            textWrap.add(label, BorderLayout.NORTH);

            add(left, BorderLayout.WEST);
            add(textWrap, BorderLayout.CENTER);

            // 宽度由 Grid 单元格拉伸填满，仅用 rowMaxWidth 估算换行
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            MouseAdapter click = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggle();
                }
            };
            MouseAdapter hover = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hoverState = true;
                    circle.repaint();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverState = false;
                    circle.repaint();
                    repaint();
                }
            };
            addMouseListener(click);
            left.addMouseListener(click);
            circle.addMouseListener(click);
            label.addMouseListener(click);
            textWrap.addMouseListener(click);
            addMouseListener(hover);
            left.addMouseListener(hover);
            circle.addMouseListener(hover);
            label.addMouseListener(hover);
            textWrap.addMouseListener(hover);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            if (hoverState && !selectedState) {
                g2.setColor(new Color(37, 99, 235, 12));
                g2.fillRoundRect(2, 3, w - 2, h - 1, 14, 14);
            }
            Color bg = Color.WHITE;
            if (selectedState) {
                bg = new Color(238, 242, 255);
            } else if (hoverState) {
                bg = new Color(250, 250, 252);
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            Color stroke = selectedState ? PRIMARY_BLUE : (hoverState ? OPTION_HOVER_BORDER : BORDER_LIGHT);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(selectedState ? 1.6f : 1f));
            g2.drawRoundRect(0, 0, w, h, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }

        private void toggle() {
            if (!isMulti) {
                answers.put(questionId, new HashSet<>(Collections.singletonList(option.getId())));
            } else {
                Set<Long> cur = answers.computeIfAbsent(questionId, k -> new HashSet<>());
                if (option.getId() != null && cur.contains(option.getId())) {
                    cur.remove(option.getId());
                } else if (option.getId() != null) {
                    cur.add(option.getId());
                }
                Set<Long> sel = answers.get(questionId);
                if (sel != null && sel.isEmpty()) {
                    answers.remove(questionId);
                }
            }
            loadQuestion(currentIndex);
            refreshExamState();
        }
    }

    // endregion
}
