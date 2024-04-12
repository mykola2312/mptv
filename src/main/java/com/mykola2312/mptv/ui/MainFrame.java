package com.mykola2312.mptv.ui;

import com.mykola2312.mptv.I18n;

import javax.swing.*;
import java.awt.*;

public class MainFrame {
    private Font font;
    private JFrame frame;
    private JList<String> categoryList;
    private JList<String> channelList;

    public void setCategories(String[] categories) {
        if (categoryList == null) {
            categoryList = new JList<>(categories);
            categoryList.setFont(font);
        } else {
            categoryList.setListData(categories);
        }
    }

    public void setChannels(String[] channels) {
        if (channelList == null) {
            channelList = new JList<>(channels);
            channelList.setFont(font);
        } else {
            channelList.setListData(channels);
        }
    }

    private void spawn(short width, short height, boolean fullscreen) {
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

        String[] items = new String[128];
        for (int i = 0; i < 128; i++) {
            items[i] = "item" + i;
        }

        setCategories(items);

        final JScrollPane categoryListScroll = new JScrollPane(categoryList);
        categoryListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        categoryListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        categoryPanel.add(categoryListScroll, BorderLayout.CENTER);

        statusPanel.add(new JButton("status"));

        setChannels(items);

        final JScrollPane channelListScroll = new JScrollPane(channelList);
        channelListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        channelListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        channelPanel.add(channelListScroll, BorderLayout.CENTER);

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

        // TEST: multi thread access
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String[] newChannels = new String[] {"first", "second"};
            SwingUtilities.invokeLater(() -> {
                setChannels(newChannels);
            });
        }).start();
    }
}
