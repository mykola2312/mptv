package com.mykola2312.mptv.piir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mykola2312.mptv.Main;
import com.mykola2312.mptv.task.TaskProcess;
import com.mykola2312.mptv.ui.MenuAction;

public class PiIR implements TaskProcess {
    private static final Logger logger = LoggerFactory.getLogger(PiIR.class);

    private final String exec;
    private final int gpio;
    private final HashMap<String, MenuAction> binds = new HashMap<>();

    private Process process = null;
    private InputStream input = null;

    private class PiIRReader implements Runnable {
        private final PiIR piir;
        private final InputStream input;
        public boolean running = true;

        public PiIRReader(PiIR piir, InputStream input) {
            this.piir = piir;
            this.input = input;
        }

        private static final int BUFFER_SIZE = 512;

        private enum ReaderState {
            SKIPPING,
            READING_JSON,
            PARSE_JSON
        }

        @Override
        public void run() {
            ReaderState state = ReaderState.SKIPPING;
            byte[] buf = new byte[BUFFER_SIZE];
            int off = 0;
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    // reader loop
                    int len = input.read(buf, off, 1);
                    if (len < 0) {
                        logger.warn("reading error. exiting");
                        running = false;
                        return;
                    }

                    switch (buf[off]) {
                        case '{' -> state = ReaderState.READING_JSON;
                        case '}' -> state = ReaderState.PARSE_JSON;
                    }

                    switch (state) {
                        case SKIPPING -> {}

                        case READING_JSON -> {
                            off++;

                            if (off >= BUFFER_SIZE) {
                                logger.warn(String.format(
                                    "buffer overflow from piir dump! %d >= %d", off, BUFFER_SIZE));
                                state = ReaderState.SKIPPING;
                                off = 0;
                            }
                        }

                        case PARSE_JSON -> {
                            String line = new String(
                                Arrays.copyOfRange(buf, 0, off+1),
                                StandardCharsets.UTF_8);
                            logger.info(line);
                            try {
                                PiIRDump dump = PiIRDump.deserialize(line);

                                piir.handleDump(dump);
                            } catch (JsonProcessingException e) {
                                logger.warn("failed to deserialize dump!", e);
                            } finally {
                                state = ReaderState.SKIPPING;
                                off = 0;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("failed to read. exiting piir reader", e);
            }
        }
    }

    private PiIRReader reader = null;
    private Thread readerThread = null;

    private static String formatBindKey(String preData, String data) {
        return preData != null ? preData + " " + data : data;
    }

    public PiIR(PiIRConfig config) {
        this.exec = config.exec;
        this.gpio = config.gpio;

        for (var bind : config.binds) {
            binds.put(formatBindKey(bind.preData, bind.data), bind.menuAction);
        }
    }

    @Override
    public boolean spawn() {
        try {
            process = Runtime.getRuntime().exec(new String[] {
                "unbuffer", exec, "dump", "--gpio", String.valueOf(gpio)
            });
        } catch (IOException e) {
            return false;
        }
        input = process.getInputStream();

        reader = new PiIRReader(this, input);
        readerThread = new Thread(reader);
        readerThread.start();

        return isAlive();
    }

    @Override
    public boolean isAlive() {
        return process != null ? process.isAlive() : false;
    }

    @Override
    public void stop() {
        if (reader != null) reader.running = false;
        if (readerThread != null) readerThread.interrupt();
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                logger.warn("failed to close input", e);
            }
        }
        if (process != null) process.destroyForcibly();
        reader = null;
        readerThread = null;
        process = null;
    }
    
    public void handleDump(PiIRDump dump) {
        String key = formatBindKey(dump.pre_data, dump.data);
        MenuAction action = binds.get(key);
        if (action != null) {
            logger.info("executing action " + action.toString());
            Main.frame.action(action);
        } else {
            logger.warn(String.format(
                "unknown piir dump pre_data \"%s\" data \"%s\"", 
                dump.pre_data, dump.data));
        }
    }
}
