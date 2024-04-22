package com.mykola2312.mptv.db;

import java.util.ArrayList;
import java.util.HashMap;

import org.jooq.UpdatableRecord;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.*;
import static com.mykola2312.mptv.tables.Category.*;
import static com.mykola2312.mptv.tables.Channel.*;

import com.mykola2312.mptv.parser.M3U;
import com.mykola2312.mptv.tables.records.ChannelRecord;

public class M3ULoader {
    private static Integer ensureRootCategory(String rootName) {
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

    public static void loadAll(ArrayList<M3U> items, String rootName) {
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
}
