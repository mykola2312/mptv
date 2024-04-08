package com.mykola2312.mptv.ui;

import com.mykola2312.mptv.I18n;

import javax.swing.*;
import java.awt.*;

public class MainFrame {
    private Font font;
    private JFrame frame;
    private JList<String> categoryList;
    private JList<String> channelList;

    public void create(short width, short height, boolean fullscreen) {
        font = new Font("Arial", Font.PLAIN, 48);

        frame = new JFrame(I18n.get("MainFrame_Title"));

        final JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JPanel categoryPanel = new JPanel(new BorderLayout());
        final JPanel channelPanel = new JPanel(new BorderLayout());

        final JSplitPane hsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryPanel, channelPanel);
        hsp.setDividerLocation(0.35);

        final JSplitPane vsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statusPanel, hsp);
        vsp.setDividerSize(2);
        vsp.setContinuousLayout(true);
        frame.add(vsp);

        categoryList = new JList<String>(new String[] {
                "category1","category2","category3","category4","category5","category6","category7","category8",
        });
        categoryList.setFont(font);
        categoryPanel.add(categoryList, BorderLayout.CENTER);

        statusPanel.add(new JButton("status"));

        channelList = new JList<String>(new String[] {
                "channel1","channel2","channel3","channel4","channel5","channel6","channel7","channel8",
        });;
        channelList.setFont(font);
        channelPanel.add(channelList, BorderLayout.CENTER);

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
