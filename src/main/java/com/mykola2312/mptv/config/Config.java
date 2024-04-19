package com.mykola2312.mptv.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {
    public FrameConfig frame;
    public DBConfig db;
    public List<SourceItem> sources;

    public static Config loadConfig(String path) throws IOException {
        return new ObjectMapper().readerFor(Config.class).readValue(new File(path));
    }
}
