package com.mykola2312.mptv.crawler;

import java.util.List;

import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;

import org.jooq.UpdatableRecord;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.*;
import static com.mykola2312.mptv.tables.Category.*;
import static com.mykola2312.mptv.tables.Channel.*;
import static com.mykola2312.mptv.tables.Source.*;

import com.mykola2312.mptv.config.SourceItem;
import com.mykola2312.mptv.parser.M3U;
import com.mykola2312.mptv.parser.M3UException;
import com.mykola2312.mptv.parser.M3UParser;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.tables.records.ChannelRecord;
import com.mykola2312.mptv.tables.records.SourceRecord;

public class Crawler {
    private static final Logger logger = Logger.getLogger(Crawler.class);

    private final List<SourceItem> sources;

    public Crawler(List<SourceItem> sources) {
        this.sources = sources;
    }

    public void updateSources(List<SourceItem> sourceItems) {
        ArrayList<UpdatableRecord<SourceRecord>> sources = new ArrayList<>();
        for (SourceItem item : sourceItems) {
            UpdatableRecord<SourceRecord> source = new UpdatableRecordImpl<>(SOURCE);
            source.set(SOURCE.TYPE, item.type.getSqlName());
            source.set(SOURCE.ROOT_NAME, item.rootCategory);
            source.set(SOURCE.URL, item.url);
            source.set(SOURCE.PATH, item.path);
            source.set(SOURCE.COOKIES, item.cookies);

            sources.add(source);
        }

        DSL.using(DB.CONFIG)
            .batchMerge(sources)
            .execute();
    }

    private Integer ensureRootCategory(String rootName) {
        try {
            return DSL.using(DB.CONFIG)
                .select(CATEGORY.ID)
                .from(CATEGORY)
                .where(CATEGORY.TITLE.eq(rootName))
                .limit(1)
                .fetchSingleInto(Integer.class);
        } catch (NoDataFoundException e) {
            return DSL.using(DB.CONFIG)
                .insertInto(CATEGORY, CATEGORY.TITLE)
                .values(rootName)
                .returningResult(CATEGORY.ID)
                .fetchSingleInto(Integer.class);
        }
    }

    public void updateAllChannels(ArrayList<M3U> items, String rootName) {
        Integer rootCategoryId = ensureRootCategory(rootName);
        HashMap<String, Integer> categories = new HashMap<>();
        // collect all groups, find or create them, cache their ids
        for (M3U item : items) {
            // no category, skip
            if (item.groupTitle == null) {
                continue;
            }
            // we already have category cached
            if (categories.get(item.groupTitle) != null) {
                continue;
            }

            Integer categoryId;
            try {
                categoryId = DSL.using(DB.CONFIG)
                    .select(CATEGORY.ID)
                    .from(CATEGORY)
                    .where(CATEGORY.TITLE.eq(item.groupTitle))
                    .limit(1)
                    .fetchSingleInto(Integer.class);
            } catch (NoDataFoundException e) {
                categoryId = DSL.using(DB.CONFIG)
                    .insertInto(CATEGORY, CATEGORY.TITLE)
                    .values(item.groupTitle)
                    .returningResult(CATEGORY.ID)
                    .fetchSingleInto(Integer.class);
            }
            categories.put(item.groupTitle, categoryId);
        }

        // upsert all channels
        ArrayList<UpdatableRecord<ChannelRecord>> channels = new ArrayList<>();
        for (M3U item : items) {
            UpdatableRecord<ChannelRecord> channel = new UpdatableRecordImpl<>(CHANNEL);
            Integer categoryId = item.groupTitle != null
                ? categories.get(item.groupTitle) : rootCategoryId;
            channel.set(CHANNEL.CATEGORY, categoryId);
            channel.set(CHANNEL.TITLE, item.title);
            channel.set(CHANNEL.URL, item.url);
            channel.set(CHANNEL.LOGO, item.tvgLogo);

            channels.add(channel);
        }
        DSL.using(DB.CONFIG)
            .batchMerge(channels)
            .execute();
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

                        updateAllChannels(m3u, source.rootCategory);
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
