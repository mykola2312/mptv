package com.mykola2312.mptv.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame {
    private JFrame frame;
    private MenuPanel menu;

    private void spawn(FrameConfig config) {
        Font font = new Font(config.fontName, Font.PLAIN, config.fontSize);

        frame = new JFrame("MPTV");

        menu = new MenuPanel(font);
        frame.add(menu, BorderLayout.CENTER);

        if (config.fullscreen) {
            frame.setUndecorated(true);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            Dimension size = new Dimension(config.width, config.height);
            frame.setPreferredSize(size);
            frame.setSize(size);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void create(FrameConfig config) {
        spawn(config);
    }

    public void loop() {
        menu.actionLoop();
    }

    public void action(MenuAction action) {
        menu.postAction(action);
    }
}
