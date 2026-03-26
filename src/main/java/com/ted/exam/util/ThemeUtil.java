package com.ted.exam.util;

import javax.swing.*;
import java.awt.*;

/**
 * 主题工具类
 * 负责设置跨版本兼容的UI外观
 */
public class ThemeUtil {
    
    /**
     * 设置系统外观，确保Windows XP/7兼容
     */
    public static void setSystemLookAndFeel() throws Exception {
        // 优先使用系统默认外观
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    
    /**
     * 获取Windows XP/7兼容的字体
     */
    public static Font getUIFont() {
        // 使用Dialog字体，这是最通用的选择
        return new Font("Dialog", Font.PLAIN, 12);
    }
    
    /**
     * 获取Windows XP/7兼容的字体（指定大小）
     */
    public static Font getUIFont(int size) {
        return new Font("Dialog", Font.PLAIN, size);
    }
    
    /**
     * 获取主色调
     * Windows XP/7兼容的蓝色调
     */
    public static Color getPrimaryColor() {
        return new Color(0, 102, 204); // Windows经典蓝色
    }
    
    /**
     * 获取次要背景色
     */
    public static Color getSecondaryBgColor() {
        return new Color(240, 240, 240);
    }
    
    /**
     * 获取文字颜色
     */
    public static Color getTextColor() {
        return new Color(51, 51, 51);
    }
    
    /**
     * 获取边框颜色
     */
    public static Color getBorderColor() {
        return new Color(153, 153, 153);
    }
    
    /**
     * 获取焦点边框颜色
     */
    public static Color getFocusBorderColor() {
        return new Color(0, 102, 204);
    }
    
    /**
     * 安全地获取屏幕尺寸
     */
    public static Dimension getScreenSize() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        // 确保至少1024x768的最小尺寸
        return new Dimension(
            Math.max(screen.width, 1024),
            Math.max(screen.height, 768)
        );
    }
    
    /**
     * 计算居中窗口位置
     */
    public static Point getCenterPoint(int width, int height) {
        Dimension screen = getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;
        return new Point(Math.max(x, 0), Math.max(y, 0));
    }
}
