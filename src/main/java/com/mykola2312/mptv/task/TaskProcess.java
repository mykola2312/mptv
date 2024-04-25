package com.mykola2312.mptv.task;

import java.io.IOException;

public interface TaskProcess {
    public void spawn() throws IOException;
    public boolean isAlive();
    public void stop();
}
