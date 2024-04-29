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
import org.newsclub.net.unix.SocketClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mykola2312.mptv.task.TaskProcess;
import com.mykola2312.mptv.task.TaskProcessState;

public class MPV implements TaskProcess {
    private static final Logger logger = LoggerFactory.getLogger(MPV.class);

    private final String url;
    private Process process;

    private TaskProcessState state = TaskProcessState.STOPPED;

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

    // around 10 seconds
    private static final int MAX_STALLED_RETRIES = 10;

    private Float lastPlaybackTime = null;
    private int stalledRetries = 0;

    private static final Path MPV_SOCKET_PATH = Path.of("/tmp/mptv-mpv.sock");
    private static final long WAIT_MILLIS = 550;
    private static final int WAIT_ATTEMPTS = 5;

    private void waitForConnection(Path socketPath) throws MPVSocketFailure {
        int attempt = 0;
        // I have to make it with while loop just to remind me
        // exit loop after socket successfuly connected
        while ((socket == null || !socket.isConnected()) && attempt++ < WAIT_ATTEMPTS) {
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
        if (state == TaskProcessState.STARTING) return false;
        try {
            state = TaskProcessState.STARTING;

            process = Runtime.getRuntime().exec(new String[] {
                "mpv", "--vo=gpu", "--ao=pulse", "--fullscreen", "--input-ipc-server=" + MPV_SOCKET_PATH, url
            });

            waitForConnection(MPV_SOCKET_PATH);

            // reset it here just to be sure
            lastPlaybackTime = null;
            stalledRetries = 0;
        } catch (IOException e) {
            return false;
        } catch (MPVSocketFailure e) {
            return false;
        }

        if (process.isAlive()) {
            state = TaskProcessState.RUNNING;
            return true;
        } else {
            state = TaskProcessState.STOPPED;
            return false;
        }
    }

    @Override
    public TaskProcessState getTaskState() {
        return state;
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
                // we need to check this because MPV loves
                // returning playback time even before video loads
                if (playbackTime == 0.0) {
                    // we're loading video, give it a try
                    stalledRetries++;
                    if (stalledRetries > MAX_STALLED_RETRIES) {
                        stalledRetries = 0;
                        return false; // we're stuck, therefore die
                    }

                    return true;
                } else {
                    boolean playbackChanged = (playbackTime - lastPlaybackTime) > 0.1;
                
                    lastPlaybackTime = playbackTime;
                    return playbackChanged;
                }
            } else { // just set first playback
                lastPlaybackTime = playbackTime;
                return true;
            }
        } catch (MPVCommandTimeout e) {
            logger.warn("mpv ipc timeout bruh");
            return false;
        } catch (MPVSocketFailure e) {
            logger.warn("since socket failure we must trigger ProcessService for restart");
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
        state = TaskProcessState.STOPPING;

        if (reader != null) reader.running = false;
        if (readerThread != null) readerThread.interrupt();
        if (socket != null) closeConnection();
        if (process != null) process.destroyForcibly();
        reader = null;
        readerThread = null;
        socket = null;
        process = null;

        state = TaskProcessState.STOPPED;
    }

    private int requestIdCounter = 0;

    final Object commandMutex = new Object();
    private int commandRequestId;
    private MPVCommandResult commandResult;

    private static final long COMMAND_TIMEOUT = 2000L;

    public MPVCommandResult executeCommand(MPVCommand command) throws MPVCommandTimeout, MPVSocketFailure {
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
        } catch (SocketClosedException e) {
            logger.warn("socket's closed");
            
            closeConnection();
            socket = null;
            
            throw new MPVSocketFailure(e);
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
