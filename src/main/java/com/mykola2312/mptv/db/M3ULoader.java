package com.mykola2312.mptv.db;

import java.util.ArrayList;
import java.util.HashMap;

import org.jooq.*;
import org.jooq.impl.*;
import static com.mykola2312.mptv.tables.Category.*;

import com.mykola2312.mptv.db.pojo.Category;
import com.mykola2312.mptv.parser.M3U;

public class M3ULoader {
    public static void loadAll(ArrayList<M3U> items) {
        // cache categories' ids
        HashMap<String, Integer> categories = new HashMap<>();
        for (M3U item : items) {
            // category
            if (item.groupTitle != null) {
                Integer id;
                Category category = DSL.using(DB.CONFIG)
                    .select()
                    .from(CATEGORY)
                    .where(CATEGORY.TITLE.eq(item.groupTitle))
                    .limit(1)
                    .fetchOne()
                    .into(Category.class);
                if (category == null) {
                    id = DSL.using(DB.CONFIG)
                        .insertInto(CATEGORY, CATEGORY.TITLE)
                        .values(item.groupTitle)
                        .returningResult(CATEGORY.ID)
                        .fetchOne()
                        .into(Integer.class);
                } else {
                    id = category.id;
                }

                categories.put(item.groupTitle, id);
            }

            // channel
        }
    }
}
