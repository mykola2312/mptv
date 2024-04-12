package com.mykola2312.mptv.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuPanel extends JPanel {
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

    static class KeyboardMenuAction extends AbstractAction {
        private final MenuPanel menu;
        private final MenuAction action;

        public KeyboardMenuAction(MenuPanel menu, MenuAction action) {
            this.menu = menu;
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            menu.handleMenuAction(action);
        }
    }

    public void setCategories(String[] categories) {
        categoryIndex = 0;
        categoryData = categories;
        if (categoryList == null) {
            categoryList = new JList<>(categoryData);
            categoryList.setFont(getFont());
        } else {
            categoryList.setListData(categoryData);
        }
    }

    public void setChannels(String[] channels) {
        channelIndex = 0;
        channelData = channels;
        if (channelList == null) {
            channelList = new JList<>(channelData);
            channelList.setFont(getFont());
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

    public MenuPanel(Font font) {
        super(new BorderLayout());
        setFont(font);

        final JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JPanel categoryPanel = new JPanel(new BorderLayout());
        final JPanel channelPanel = new JPanel(new BorderLayout());

        final JSplitPane hsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryPanel, channelPanel);
        hsp.setDividerLocation(0.35);

        final JSplitPane vsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statusPanel, hsp);
        vsp.setDividerSize(2);
        vsp.setContinuousLayout(true);
        add(vsp);

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
        //JRootPane rootPane = panel.getRootPane();
        getInputMap(IFW).put(KeyStroke.getKeyStroke("W"), "W");
        getInputMap(IFW).put(KeyStroke.getKeyStroke("S"), "S");
        getInputMap(IFW).put(KeyStroke.getKeyStroke("A"), "A");
        getInputMap(IFW).put(KeyStroke.getKeyStroke("D"), "D");

        getActionMap().put("W", new KeyboardMenuAction(this, MenuAction.ACTION_UP));
        getActionMap().put("S", new KeyboardMenuAction(this, MenuAction.ACTION_DOWN));
        getActionMap().put("A", new KeyboardMenuAction(this, MenuAction.ACTION_LEFT));
        getActionMap().put("D", new KeyboardMenuAction(this, MenuAction.ACTION_RIGHT));

        menuPosition = MenuPosition.MENU_CATEGORIES;
    }
}
