package com.mykola2312.mptv.crawler;

import java.util.List;

import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.IOException;

import com.mykola2312.mptv.config.SourceItem;
import com.mykola2312.mptv.parser.M3U;
import com.mykola2312.mptv.parser.M3UException;
import com.mykola2312.mptv.parser.M3UParser;

public class Crawler {
    private static final Logger logger = Logger.getLogger(Crawler.class);

    private final List<SourceItem> sources;

    public Crawler(List<SourceItem> sources) {
        this.sources = sources;
    }

    public void crawl() {
        for (SourceItem source : sources) {
            switch (source.type) {
                case M3U_LOCAL -> {
                    try {
                        if (source.path == null) {
                            logger.error("m3u local has to have \"path\" variable");
                            continue;
                        } else if (source.rootCategory == null) {
                            logger.error("source has to have \"rootCategory\"");
                            continue;
                        }

                        String m3uData = Files.readString(Paths.get(source.path), StandardCharsets.UTF_8);
                        ArrayList<M3U> m3u = M3UParser.parse(m3uData);

                        M3ULoader.loadAll(m3u, source.rootCategory);
                    } catch (IOException e) {
                        logger.error(e);
                        logger.error(String.format("failed to read local m3u file: %s", e.getMessage()));
                    } catch (M3UException e) {
                        logger.error(e);
                        logger.error(String.format("failed to parse m3u: %s", e.getMessage()));
                    }
                }

                default -> {
                    logger.error(String.format("source type %s is not implemented yet :(", source.type.name()));
                }
            }
        }
    }
}
