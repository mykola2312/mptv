package com.mykola2312.mptv.piir;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

public class PiIRConfig {
    @NonNull
    public String exec;

    public int gpio;

    @NonNull    
    public List<PiIRBindItem> binds;
}
