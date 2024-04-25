package com.mykola2312.mptv.mpv;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;

import org.newsclub.net.unix.AFOutputStream;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MPVSocket {
    private static final Logger logger = LoggerFactory.getLogger(MPVSocket.class);

    private AFUNIXSocket socket;

    private static final long WAIT_MILLIS = 250;
    private static final int WAIT_ATTEMPTS = 5;

    public void waitForConnection(Path socketPath) throws IOException {
        for (int i = 0; i < WAIT_ATTEMPTS; i++) {
            try {
                Thread.sleep(WAIT_MILLIS);

                socket = AFUNIXSocket.newInstance();
                socket.connect(AFUNIXSocketAddress.of(socketPath));
                
                logger.info(String.format("connected to socket %s", socket.toString()));
            } catch (SocketException e) {
                logger.error("SocketException", e);
                close();
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("failed to close socket", e);
        }
    }

    public void writeCommandRaw(MPVCommandRaw command) {
        try {
            AFOutputStream output = socket.getOutputStream();

            output.write(command.serialize());
            output.write('\n');
            output.flush();
        } catch (JsonProcessingException e) {
            logger.error("failed to serialize command", e);
        } catch (IOException e) {
            logger.error("io exception", e);
        }
    }
}
