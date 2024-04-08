package com.mykola2312.mptv.ui;

import javax.swing.*;

public class MainFrame {
    private JFrame frame;

    public void create() {
        this.frame = new JFrame("MPTV");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
