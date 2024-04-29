package com.mykola2312.mptv.mpv;

public class MPVSocketFailure extends RuntimeException {
    public MPVSocketFailure(Throwable cause) {
        super("fatal IPC failure. cannot continue");
    }
}
