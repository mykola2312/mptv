package com.mykola2312.mptv.mpv;

public class MPVCommandTimeout extends RuntimeException {
    public MPVCommandTimeout() {
        super("mpv command response timeout");
    }
}
