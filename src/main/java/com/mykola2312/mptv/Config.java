package com.mykola2312.mptv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykola2312.mptv.crawler.SourceItem;
import com.mykola2312.mptv.db.DBConfig;
import com.mykola2312.mptv.piir.PiIRConfig;
import com.mykola2312.mptv.task.TaskItem;
import com.mykola2312.mptv.ui.FrameConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {
    public FrameConfig frame;
    public DBConfig db;
    public PiIRConfig piir;
    public List<SourceItem> sources;
    public List<TaskItem> tasks;

    public static Config loadConfig(String path) throws IOException {
        return new ObjectMapper().readerFor(Config.class).readValue(new File(path));
    }
}
