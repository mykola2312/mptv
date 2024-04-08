package com.mykola2312.mptv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.File;
import java.io.IOException;

public class Config {

    public class Frame {
        public short width;
        public short height;
        public boolean fullscreen;

    }
    public Frame frame;

    public static Config loadConfig(String path) throws IOException {
        return new ObjectMapper().readerFor(Config.class).readValue(new File(path));
    }
}
