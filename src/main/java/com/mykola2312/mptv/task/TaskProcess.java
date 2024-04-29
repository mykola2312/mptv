package com.mykola2312.mptv.task;

public interface TaskProcess {
    public boolean spawn();
    public boolean isAlive();
    public void stop();
}
