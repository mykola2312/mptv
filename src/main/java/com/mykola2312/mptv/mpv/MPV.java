package com.mykola2312.mptv.mpv;

import java.io.IOException;
import java.nio.file.Path;

import com.mykola2312.mptv.task.TaskProcess;

public class MPV implements TaskProcess {
    private final String url;
    private Process process;
    private MPVSocket socket = null;

    public MPV(String url) throws IOException {
        this.url = url;
    }

    private static final Path MPV_SOCKET_PATH = Path.of("/tmp/mptv-mpv.sock");

    @Override
    public boolean spawn() throws IOException {
        // to prevent possible file descriptor leaks
        if (socket != null) {
            socket.close();
            socket = null;
        }

        process = Runtime.getRuntime().exec(new String[] {
            "mpv", url, "--input-ipc-server=" + MPV_SOCKET_PATH
        });

        socket = new MPVSocket();
        socket.waitForConnection(MPV_SOCKET_PATH);

        // TODO: remove test code
        socket.writeCommandRaw(new MPVCommandRaw("set_property", "volume", "0"));

        return isAlive();
    }

    @Override
    public boolean isAlive() {
        return process.isAlive();
    }

    @Override
    public void stop() {
        socket.close();
        socket = null;
    
        process.destroyForcibly();
    }
}
