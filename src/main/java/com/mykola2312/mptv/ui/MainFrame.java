package com.mykola2312.mptv.ui;

import com.mykola2312.mptv.I18n;

import javax.swing.*;
import java.awt.*;

public class MainFrame {
    private JFrame frame;

    public void create(short width, short height, boolean fullscreen) {
        frame = new JFrame(I18n.get("MainFrame_Title"));

        JPanel categoryPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        JPanel statusPanel = new JPanel();
        JPanel channelPanel = new JPanel();
        JSplitPane vsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statusPanel, channelPanel);
        rightPanel.add(vsp);

        JSplitPane hsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryPanel, rightPanel);
        hsp.setDividerLocation(0.30);
        frame.add(hsp);

        categoryPanel.add(new JButton("category"));
        statusPanel.add(new JButton("status"));
        channelPanel.add(new JButton("channel"));


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
}
