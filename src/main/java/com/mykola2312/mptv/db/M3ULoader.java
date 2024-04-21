package com.mykola2312.mptv.db;

import java.util.ArrayList;
import java.util.HashMap;

import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.*;
import static com.mykola2312.mptv.tables.Category.*;
import static com.mykola2312.mptv.tables.Channel.*;

import com.mykola2312.mptv.parser.M3U;

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
        // cache categories' ids
        HashMap<String, Integer> categories = new HashMap<>();
        for (M3U item : items) {
            // category
            Integer categoryId;
            if (item.groupTitle != null) {
                categoryId = categories.get(item.groupTitle);
                if (categoryId == null) {
                    Integer id;
                    try {
                        id = DSL.using(DB.CONFIG)
                            .select(CATEGORY.ID)
                            .from(CATEGORY)
                            .where(CATEGORY.TITLE.eq(item.groupTitle))
                            .limit(1)
                            .fetchSingleInto(Integer.class);
                    } catch (NoDataFoundException e) {
                        id = DSL.using(DB.CONFIG)
                            .insertInto(CATEGORY, CATEGORY.TITLE)
                            .values(item.groupTitle)
                            .returningResult(CATEGORY.ID)
                            .fetchOne()
                            .into(Integer.class);
                    }

                    categories.put(item.groupTitle, id);
                    categoryId = id;
                }
            } else {
                categoryId = rootCategoryId;
            }

            // channel
            try {
                Integer channelId = DSL.using(DB.CONFIG)
                    .select(CHANNEL.ID)
                    .from(CHANNEL)
                    .where(CHANNEL.CATEGORY.eq(categoryId)
                        .and(CHANNEL.TITLE.eq(item.title)))
                    .limit(1)
                    .fetchSingleInto(Integer.class);
                
                DSL.using(DB.CONFIG)
                    .update(CHANNEL)
                    .set(CHANNEL.URL, item.url)
                    .set(CHANNEL.LOGO, item.tvgLogo)
                    .where(CHANNEL.ID.eq(channelId))
                    .execute();
            } catch (NoDataFoundException e) {
                DSL.using(DB.CONFIG)
                    .insertInto(CHANNEL,
                        CHANNEL.CATEGORY, CHANNEL.TITLE,
                        CHANNEL.URL, CHANNEL.LOGO)
                    .values(categoryId, item.title, item.url, item.tvgLogo)
                    .execute();
            }
        }
    }
}
