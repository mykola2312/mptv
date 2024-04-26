package com.mykola2312.mptv.mpv;

public enum MPVProperty {
    VOLUME ("volume");
    
    private final String name;
    
    private MPVProperty(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
