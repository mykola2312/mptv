package com.mykola2312.mptv.mpv;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;

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
        private final AFInputStream input;
        public boolean running = true;

        public MPVReader(AFInputStream input) {
            this.input = input;
        }

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {

            }
        }
        
    }

    private MPVReader reader;
    private Thread readerThread;

    private static final Path MPV_SOCKET_PATH = Path.of("/tmp/mptv-mpv.sock");
    private static final long WAIT_MILLIS = 250;
    private static final int WAIT_ATTEMPTS = 5;

    public void waitForConnection(Path socketPath) throws IOException {
        for (int i = 0; i < WAIT_ATTEMPTS; i++) {
            try {
                Thread.sleep(WAIT_MILLIS);

                socket = AFUNIXSocket.newInstance();
                socket.connect(AFUNIXSocketAddress.of(socketPath));

                reader = new MPVReader(socket.getInputStream());
                readerThread = new Thread(reader);
                readerThread.start();
                
                logger.info(String.format("connected to socket %s", socket.toString()));
            } catch (SocketException e) {
                logger.error("SocketException", e);
                closeConnection();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("failed to close socket", e);
        }
    }

    @Override
    public boolean spawn() throws IOException {
        process = Runtime.getRuntime().exec(new String[] {
            "mpv", url, "--input-ipc-server=" + MPV_SOCKET_PATH
        });

        waitForConnection(MPV_SOCKET_PATH);

        writeCommand(new MPVSetProperty(MPVProperty.VOLUME, 0));

        return isAlive();
    }

    @Override
    public boolean isAlive() {
        return process.isAlive();
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

    public void writeCommand(MPVCommand command) {
        try {
            AFOutputStream output = socket.getOutputStream();
            
            output.write(command.serialize());
            output.flush();
        } catch (JsonProcessingException e) {
            logger.error("failed to serialize command", e);
        } catch (IOException e) {
            logger.error("io exception", e);
        }
    }
}
