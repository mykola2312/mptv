package com.mykola2312.mptv.ui;

import com.mykola2312.mptv.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;

public class MainFrame {
    private Font font;
    private JFrame frame;
    private String[] categoryData;
    private JList<String> categoryList;

    private String[] channelData;
    private JList<String> channelList;

    enum MenuPosition {
        MENU_CATEGORIES,
        MENU_CHANNELS
    }

    private MenuPosition menuPosition;
    private int categoryIndex;
    private int channelIndex;

    enum MenuAction {
        ACTION_UP,
        ACTION_DOWN,
        ACTION_LEFT,
        ACTION_RIGHT
    }

    class KeyboardMenuAction extends AbstractAction {
        private final MainFrame frame;
        private final MenuAction action;

        public KeyboardMenuAction(MainFrame frame, MenuAction action) {
            this.frame = frame;
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            frame.handleMenuAction(action);
        }
    }

    public void setCategories(String[] categories) {
        categoryIndex = 0;
        categoryData = categories;
        if (categoryList == null) {
            categoryList = new JList<>(categoryData);
            categoryList.setFont(font);
        } else {
            categoryList.setListData(categoryData);
        }
    }

    public void setChannels(String[] channels) {
        channelIndex = 0;
        channelData = channels;
        if (channelList == null) {
            channelList = new JList<>(channelData);
            channelList.setFont(font);
        } else {
            channelList.setListData(channelData);
        }
    }

    public void handleMenuAction(MenuAction action) {
        switch (action) {
            case ACTION_UP -> {
                switch (menuPosition) {
                    case MENU_CATEGORIES -> categoryIndex = (categoryIndex - 1) % categoryData.length;
                    case MENU_CHANNELS -> channelIndex = (channelIndex - 1) % channelData.length;
                }
            }
            case ACTION_DOWN ->  {
                switch (menuPosition) {
                    case MENU_CATEGORIES -> categoryIndex = (categoryIndex + 1) % categoryData.length;
                    case MENU_CHANNELS -> channelIndex = (channelIndex + 1) % channelData.length;
                }
            }
            case ACTION_LEFT -> menuPosition = MenuPosition.MENU_CATEGORIES;
            case ACTION_RIGHT -> menuPosition = MenuPosition.MENU_CHANNELS;
        }

        switch (menuPosition) {
            case MENU_CATEGORIES -> {
                categoryList.setSelectedIndex(categoryIndex);
                categoryList.ensureIndexIsVisible(categoryIndex);
            }
            case MENU_CHANNELS -> {
                channelList.setSelectedIndex(channelIndex);
                channelList.ensureIndexIsVisible(channelIndex);
            }
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

        final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
        JRootPane rootPane = frame.getRootPane();
        rootPane.getInputMap().put(KeyStroke.getKeyStroke("W"), "W");
        rootPane.getInputMap().put(KeyStroke.getKeyStroke("S"), "S");
        rootPane.getInputMap().put(KeyStroke.getKeyStroke("A"), "A");
        rootPane.getInputMap().put(KeyStroke.getKeyStroke("D"), "D");

        rootPane.getActionMap().put("W", new KeyboardMenuAction(this, MenuAction.ACTION_UP));
        rootPane.getActionMap().put("S", new KeyboardMenuAction(this, MenuAction.ACTION_DOWN));
        rootPane.getActionMap().put("A", new KeyboardMenuAction(this, MenuAction.ACTION_LEFT));
        rootPane.getActionMap().put("D", new KeyboardMenuAction(this, MenuAction.ACTION_RIGHT));

        menuPosition = MenuPosition.MENU_CATEGORIES;

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
