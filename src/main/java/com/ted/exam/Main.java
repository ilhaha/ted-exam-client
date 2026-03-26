package com.ted.exam;

import com.ted.exam.ui.LoginFrame;
import com.ted.exam.util.ThemeUtil;

import javax.swing.*;

/**
 * 考试客户端主程序入口
 * 兼容Windows XP/7及以上系统
 */
public class Main {
    
    public static void main(String[] args) {
        // 设置外观主题适配老系统
        try {
            ThemeUtil.setSystemLookAndFeel();
        } catch (Exception e) {
            System.err.println("设置系统外观失败，使用默认外观: " + e.getMessage());
        }
        
        // 在EDT线程上创建GUI
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
