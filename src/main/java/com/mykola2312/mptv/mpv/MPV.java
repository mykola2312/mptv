package com.mykola2312.mptv.mpv;

import java.io.IOException;

import com.mykola2312.mptv.task.TaskProcess;

public class MPV implements TaskProcess {
    private final String url;
    private Process process;

    public MPV(String url) throws IOException {
        this.url = url;
        spawn();
    }

    @Override
    public void spawn() throws IOException {
        process = Runtime.getRuntime().exec(new String[] {
            "mpv", url
        });
    }

    @Override
    public boolean isAlive() {
        return process.isAlive();
    }

    @Override
    public void stop() {
        process.destroyForcibly();
    }
}
