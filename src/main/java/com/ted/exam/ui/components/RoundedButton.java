package com.ted.exam.ui.components;

import com.ted.exam.util.ThemeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 圆角按钮组件
 * Windows XP/7兼容版本
 */
public class RoundedButton extends JButton {
    
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private int cornerRadius = 6;
    
    public RoundedButton(String text) {
        super(text);
        init();
    }
    
    public RoundedButton(String text, int cornerRadius) {
        super(text);
        this.cornerRadius = cornerRadius;
        init();
    }
    
    private void init() {
        // 设置按钮颜色
        normalColor = ThemeUtil.getPrimaryColor();
        hoverColor = new Color(0, 120, 215);
        pressedColor = new Color(0, 80, 160);
        textColor = Color.WHITE;
        
        // 按钮基本设置
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(textColor);
        setFont(new Font("Dialog", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 设置内边距
        setMargin(new Insets(10, 25, 10, 25));
        
        // 启用鼠标事件监听
        enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 确定当前状态的颜色
        Color currentColor = normalColor;
        if (getModel().isPressed()) {
            currentColor = pressedColor;
        } else if (getModel().isRollover()) {
            currentColor = hoverColor;
        }
        
        // 绘制圆角矩形背景
        Shape shape = new RoundRectangle2D.Float(
            0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius
        );
        g2.setColor(currentColor);
        g2.fill(shape);
        
        // 绘制文字
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2 - 2;
        g2.setFont(getFont());
        g2.drawString(text, x, y);
        
        g2.dispose();
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // 不绘制边框
    }
    
    /**
     * 设置按钮主题颜色
     */
    public void setThemeColor(Color normal, Color hover, Color pressed) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = pressed;
        repaint();
    }
    
    /**
     * 设置圆角半径
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
}
