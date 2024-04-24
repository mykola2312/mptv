package com.mykola2312.mptv.ui;

import javax.swing.*;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jooq.impl.DSL;

import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.db.pojo.Category;
import com.mykola2312.mptv.db.pojo.Channel;

import static com.mykola2312.mptv.tables.Category.*;
import static com.mykola2312.mptv.tables.Channel.*;

import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuPanel extends JPanel {
    private List<Category> categoryData;
    private JList<Category> categoryList;

    private List<Channel> channelData;
    private JList<Channel> channelList;

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

    private void loadCategories() {
        categoryData = DSL.using(DB.CONFIG)
            .select(CATEGORY.ID, CATEGORY.TITLE)
            .from(CATEGORY)
            .fetchInto(Category.class);
        categoryList.setListData(categoryData.toArray(new Category[0]));
    }

    private void loadChannels(Integer categoryId) {
        channelData = DSL.using(DB.CONFIG)
            .select(CHANNEL.ID, CHANNEL.TITLE)
            .from(CHANNEL)
            .where(CHANNEL.CATEGORY.eq(categoryId))
            .fetchInto(Channel.class);
        channelList.setListData(channelData.toArray(new Channel[0]));
    }

    public void handleMenuAction(MenuAction action) {
        switch (action) {
            case ACTION_UP -> {
                switch (menuPosition) {
                    case MENU_CATEGORIES -> categoryIndex--;
                    case MENU_CHANNELS -> channelIndex--;
                }
            }
            case ACTION_DOWN ->  {
                switch (menuPosition) {
                    case MENU_CATEGORIES -> categoryIndex++;
                    case MENU_CHANNELS -> channelIndex++;
                }
            }
            case ACTION_LEFT -> menuPosition = MenuPosition.MENU_CATEGORIES;
            case ACTION_RIGHT -> menuPosition = MenuPosition.MENU_CHANNELS;
        }
        if (categoryIndex < 0) categoryIndex = 0;
        if (channelIndex < 0) channelIndex = 0;
        
        if (categoryData != null) categoryIndex = categoryIndex % categoryData.size();
        if (channelData != null) channelIndex = channelIndex % channelData.size();

        switch (menuPosition) {
            case MENU_CATEGORIES -> {
                loadCategories();
                channelIndex = 0;

                categoryList.setEnabled(true);
                channelList.setEnabled(false);

                categoryList.setSelectedIndex(categoryIndex);
                categoryList.ensureIndexIsVisible(categoryIndex);
            }
            case MENU_CHANNELS -> {
                loadChannels(categoryData.get(categoryIndex).id);

                categoryList.setEnabled(false);
                channelList.setEnabled(true);

                channelList.setSelectedIndex(channelIndex);
                channelList.ensureIndexIsVisible(channelIndex);
            }
        }
    }

    public MenuPanel(Font font) {
        super(new BorderLayout());
        setFont(font);

        //final LogPanel logPanel = new LogPanel(LoggerFactory.getRootLogger());
        final JPanel logPanel = new JPanel();
        final JPanel categoryPanel = new JPanel(new BorderLayout());
        final JPanel channelPanel = new JPanel(new BorderLayout());

        final JSplitPane hsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryPanel, channelPanel);
        hsp.setDividerLocation(0.35);

        final JSplitPane vsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logPanel, hsp);
        vsp.setDividerSize(2);
        vsp.setContinuousLayout(true);
        vsp.setDividerLocation(50);
        add(vsp);

        categoryList = new JList<Category>();
        categoryList.setFont(getFont());

        final JScrollPane categoryListScroll = new JScrollPane(categoryList);
        categoryListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        categoryListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        categoryPanel.add(categoryListScroll, BorderLayout.CENTER);

        channelList = new JList<Channel>();
        channelList.setFont(getFont());

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
        categoryList.setEnabled(true);
        channelList.setEnabled(false);

        loadCategories();
    }
}
