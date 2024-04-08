package com.mykola2312.mptv.ui;

import com.mykola2312.mptv.I18n;

import javax.swing.*;

public class MainFrame {
    private JFrame frame;

    public void create() {
        this.frame = new JFrame(I18n.get("MainFrame_Title"));

        frame.setUndecorated(true);
        frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
