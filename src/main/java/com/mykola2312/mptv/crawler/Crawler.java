package com.mykola2312.mptv.crawler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;

import org.jooq.UpdatableRecord;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.*;
import static com.mykola2312.mptv.tables.Category.*;
import static com.mykola2312.mptv.tables.Channel.*;
import static com.mykola2312.mptv.tables.Source.*;
import static com.mykola2312.mptv.tables.Crawl.*;

import com.mykola2312.mptv.parser.M3U;
import com.mykola2312.mptv.parser.M3UException;
import com.mykola2312.mptv.parser.M3UParser;
import com.mykola2312.mptv.db.DB;
import com.mykola2312.mptv.db.pojo.Source;
import com.mykola2312.mptv.tables.records.ChannelRecord;
import com.mykola2312.mptv.tables.records.SourceRecord;
import com.mykola2312.mptv.task.Task;

public class Crawler implements Task {
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

    private Integer crawlId;

    public Crawler() {
    }

    public void updateSources(List<SourceItem> sourceItems) {
        ArrayList<UpdatableRecord<SourceRecord>> sources = new ArrayList<>(sourceItems.size());
        for (SourceItem item : sourceItems) {
            UpdatableRecord<SourceRecord> source = new UpdatableRecordImpl<>(SOURCE);
            source.set(SOURCE.TYPE, item.type.getSqlName());
            source.set(SOURCE.ROOT_NAME, item.rootCategory);
            source.set(SOURCE.URL_OR_PATH, item.url != null ? item.url : item.path);
            source.set(SOURCE.COOKIES, item.cookies);

            sources.add(source);
        }

        DSL.using(DB.CONFIG)
            .batchMerge(sources)
            .execute();
    }

    private List<Source> loadSources() {
        return DSL.using(DB.CONFIG)
            .select()
            .from(SOURCE)
            .fetchInto(Source.class);
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
        ArrayList<UpdatableRecord<ChannelRecord>> channels = new ArrayList<>(items.size());
        for (M3U item : items) {
            UpdatableRecord<ChannelRecord> channel = new UpdatableRecordImpl<>(CHANNEL);
            Integer categoryId = item.groupTitle != null
                ? categories.get(item.groupTitle) : rootCategoryId;
            channel.set(CHANNEL.CATEGORY, categoryId);
            channel.set(CHANNEL.TITLE, item.title);
            channel.set(CHANNEL.URL, item.url);
            channel.set(CHANNEL.LOGO, item.tvgLogo);
            channel.set(CHANNEL.CRAWL, crawlId);

            channels.add(channel);
        }
        DSL.using(DB.CONFIG)
            .batchMerge(channels)
            .execute();
    }

    private Integer beginCrawl() {
        return DSL.using(DB.CONFIG)
            .insertInto(CRAWL, CRAWL.CRAWLED_AT)
            .values(Instant.now().toEpochMilli() / 1000L)
            .returningResult(CRAWL.ID)
            .fetchSingleInto(Integer.class);
    }

    public void crawl() {
        crawlId = beginCrawl();
        for (Source source : loadSources()) {
            if (source.urlOrPath == null) {
                logger.error("m3u local has to have \"path\" variable");
                continue;
            } else if (source.rootName == null) {
                logger.error("source has to have \"rootCategory\"");
                continue;
            }

            switch (source.type) {
                case "m3u-local" -> {
                    try {
                        String m3uData = Files.readString(Paths.get(source.urlOrPath), StandardCharsets.UTF_8);
                        ArrayList<M3U> m3u = M3UParser.parse(m3uData);

                        updateAllChannels(m3u, source.rootName);
                    } catch (IOException e) {
                        logger.error(e.toString());
                        logger.error(String.format("failed to read local m3u file: %s", e.getMessage()));
                    } catch (M3UException e) {
                        logger.error("failed to parse m3u", e);
                    }
                }

                case "m3u" -> {
                    try {
                        WebRequest get = new WebRequest(source.urlOrPath);
                        WebContent content = get.fetch();
                        ArrayList<M3U> m3u = M3UParser.parse(content.body);

                        updateAllChannels(m3u, source.rootName);
                    } catch (WebException e) {
                        logger.warn("failed to fetch " + source.urlOrPath);
                    } catch (M3UException e) {
                        logger.error("failed to parse m3u", e);
                    }
                }

                default -> {
                    logger.error(String.format("source type %s is not implemented yet :(", source.type));
                }
            }
        }
    }

    private static final String TASK_NAME = "crawler";

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public void dispatch() {
        crawl();
    }
}
