package com.mykola2312.mptv.mpv;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.newsclub.net.unix.AFInputStream;
import org.newsclub.net.unix.AFOutputStream;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mykola2312.mptv.task.TaskProcess;

public class MPV implements TaskProcess {
    private static final Logger logger = LoggerFactory.getLogger(MPV.class);

    private final String url;
    private Process process;

    public MPV(String url) {
        this.url = url;
    }

    private AFUNIXSocket socket;

    private class MPVReader implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(MPVReader.class);

        private final MPV mpv;
        private final AFInputStream input;
        public boolean running = true;

        public MPVReader(MPV mpv, AFInputStream input) {
            this.mpv = mpv;
            this.input = input;
        }

        private static final int BUFFER_SIZE = 512;
        private static final Pattern EVENT_PATTERN = Pattern.compile("^\\{\"event\"");
        
        @Override
        public void run() {
            byte[] buf = new byte[BUFFER_SIZE];
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    int len = input.read(buf, 0, buf.length);
                    if (len < 0) {
                        running = false;
                        return;
                    }

                    String line = new String(
                        Arrays.copyOfRange(buf, 0, len),
                        StandardCharsets.UTF_8);
                    
                    Matcher eventMatch = EVENT_PATTERN.matcher(line);
                    if (eventMatch.find()) {
                        // handle event
                    } else {
                        // handle command result
                        try {
                            MPVCommandResult result = MPVCommandResult.deserialize(line);
                            mpv.handleCommandResult(result);
                        } catch (JsonProcessingException e) {
                            logger.warn("failed to parse: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("failed to read. exiting reader", e);
            }
        }
        
    }

    private MPVReader reader;
    private Thread readerThread;

    private Float lastPlaybackTime = null;

    private static final Path MPV_SOCKET_PATH = Path.of("/tmp/mptv-mpv.sock");
    private static final long WAIT_MILLIS = 250;
    private static final int WAIT_ATTEMPTS = 5;

    private void waitForConnection(Path socketPath) throws MPVSocketFailure {
        for (int i = 0; i < WAIT_ATTEMPTS; i++) {
            try {
                Thread.sleep(WAIT_MILLIS);

                socket = AFUNIXSocket.newInstance();
                socket.connect(AFUNIXSocketAddress.of(socketPath));

                reader = new MPVReader(this, socket.getInputStream());
                readerThread = new Thread(reader);
                readerThread.start();
                
                logger.info(String.format("connected to socket %s", socket.toString()));
            } catch (SocketException e) {
                logger.info("SocketException.. trying to connect");
                closeConnection();
            } catch (IOException e) {
                throw new MPVSocketFailure(e);
            } catch (InterruptedException e) {
                throw new MPVSocketFailure(e);
            }
        }
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("failed to close socket", e);
        }
    }

    @Override
    public boolean spawn() {
        try {
            process = Runtime.getRuntime().exec(new String[] {
                "mpv", "--vo=gpu", "--ao=pulse", "--fullscreen", "--input-ipc-server=" + MPV_SOCKET_PATH, url
            });

            waitForConnection(MPV_SOCKET_PATH);
        } catch (IOException e) {
            return false;
        } catch (MPVSocketFailure e) {
            return false;
        }

        return process.isAlive();
    }

    private boolean checkPlayback() {
        try {
            // get playback
            MPVCommandResult result = executeCommand(
                new MPVGetProperty(MPVProperty.PLAYBACK_TIME));
            
            Float playbackTime;
            try {
                playbackTime = Float.parseFloat(result.data);
            } catch (NumberFormatException e) {
                logger.error("FAILED TO PARSE PLAYBACK DATA: " + result.data);
                return false;
            }
            logger.info("playbackTime " + playbackTime);

            // if we have previous playback - compare them,
            // if not changed, then player stuck
            if (lastPlaybackTime != null) {
                boolean playbackChanged = (playbackTime - lastPlaybackTime) > 0.1;
                
                lastPlaybackTime = playbackTime;
                return playbackChanged;
            } else { // just set first playback
                lastPlaybackTime = playbackTime;
                return true;
            }
        } catch (MPVCommandTimeout e) {
            logger.warn("mpv ipc timeout bruh");
            return false;
        }
    }

    @Override
    public boolean isAlive() {
        // if we have process, check if playback still going on
        if (process != null) {
            return checkPlayback();
        } else {
            return false;
        }
    }

    @Override
    public void stop() {
        if (reader != null) reader.running = false;
        if (readerThread != null) readerThread.interrupt();
        if (socket != null) closeConnection();
        if (process != null) process.destroyForcibly();
        reader = null;
        readerThread = null;
        socket = null;
        process = null;
    }

    private int requestIdCounter = 0;

    final Object commandMutex = new Object();
    private int commandRequestId;
    private MPVCommandResult commandResult;

    private static final long COMMAND_TIMEOUT = 2000L;

    public MPVCommandResult executeCommand(MPVCommand command) {
        try {
            commandRequestId = command.setRequestId(requestIdCounter++);

            AFOutputStream output = socket.getOutputStream();
            
            output.write(command.serialize());
            output.flush();

            // wait for command result
            synchronized (commandMutex) {
                try {
                    commandMutex.wait(COMMAND_TIMEOUT);
                } catch (InterruptedException e) {
                    throw new MPVCommandTimeout();
                }
            }

            return commandResult;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to serialize command", e);
        } catch (IOException e) {
            throw new RuntimeException("io exception", e);
        }
    }

    public void handleCommandResult(MPVCommandResult result) {
        synchronized (commandMutex) {
            if (commandRequestId == result.request_id) {
                commandResult = result;
                commandMutex.notifyAll();
            }
        }
    }
}
