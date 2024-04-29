package com.mykola2312.mptv.task;

public interface TaskProcess {
    public boolean spawn();
    public TaskProcessState getTaskState();
    public boolean isAlive();
    public void stop();
}
