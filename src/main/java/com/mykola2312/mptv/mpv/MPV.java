package com.mykola2312.mptv.mpv;

import java.io.IOException;

public class MPV {
    private final Process process;

    public MPV(String url) throws IOException {
        process = Runtime.getRuntime().exec(new String[] {
            "mpv", url
        });
    }

    public void stop() {
        process.destroyForcibly();
    }
}
