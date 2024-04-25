package com.mykola2312.mptv.task;

import java.io.IOException;

public interface TaskProcess {
    public boolean spawn() throws IOException;
    public boolean isAlive();
    public void stop();
}
