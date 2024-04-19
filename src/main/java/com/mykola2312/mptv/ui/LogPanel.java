package com.mykola2312.mptv.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.Filter;;

public class LogPanel extends JPanel {
    private class LogAppender extends AppenderSkeleton {
        private final LogPanel area;
        
        public LogAppender(LogPanel area) {
            super();
            this.area = area;
            
            this.addFilter(new Filter() {
                @Override()
                public int decide(LoggingEvent event) {
                    if (event.getLevel().equals(Level.DEBUG)) {
                        return DENY;
                    } else {
                        return ACCEPT;
                    }
                }
            });
        }

        @Override
        public void close() {}

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(LoggingEvent event) {
            area.appendEvent(event);
        }
    }

    private final JTextArea logArea;
    private final JScrollPane scrollLog;

    public void appendEvent(LoggingEvent event) {
        String content = event.getRenderedMessage();
        logArea.append(content + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public LogPanel(Logger target) {
        super(new BorderLayout());
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);

        scrollLog = new JScrollPane(logArea);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollLog, BorderLayout.CENTER);

        target.addAppender(new LogAppender(this));
    }
}
