package com.mykola2312.mptv.config;

import org.checkerframework.checker.nullness.qual.*;

public class TaskItem {
    @NonNull
    public String name;

    @NonNull
    public int interval; // in seconds
}
