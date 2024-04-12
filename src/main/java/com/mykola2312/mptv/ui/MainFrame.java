package com.mykola2312.mptv.ui;

import com.mykola2312.mptv.I18n;

import javax.swing.*;
import java.awt.*;

public class MainFrame {
    private JFrame frame;
    private MenuPanel menu;

    private void spawn(short width, short height, boolean fullscreen) {
        Font font = new Font("Arial", Font.PLAIN, 48);

        frame = new JFrame(I18n.get("MainFrame_Title"));

        menu = new MenuPanel(font);
        frame.add(menu, BorderLayout.CENTER);

        if (fullscreen) {
            frame.setUndecorated(true);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            Dimension size = new Dimension(width, height);
            frame.setPreferredSize(size);
            frame.setSize(size);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void create(short width, short height, boolean fullscreen) {
        SwingUtilities.invokeLater(() -> {
            spawn(width, height, fullscreen);
        });
    }
}
