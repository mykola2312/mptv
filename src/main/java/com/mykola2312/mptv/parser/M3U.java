package com.mykola2312.mptv.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;

public class M3U {
    @NonNull
    public String title;

    public String groupTitle;
    public String tvgLogo;

    @NonNull
    public String url;

    public M3U(String tags, String title) {
        parseTags(tags);
        this.title = title;
    }

    private static final Pattern EXTINF_TAG = Pattern.compile("^(.*)=\"(.*)\"");

    private void parseTags(String tags) {
        for (String tag : tags.split(" ")) {
            Matcher matcher = EXTINF_TAG.matcher(tag);
            if (matcher.find()) {
                String name = matcher.group(1);
                String value = matcher.group(2);

                switch (name) {
                    case "group-title" -> groupTitle = value;
                    case "tvg-logo" -> tvgLogo = value;
                }
            }
        }
    }
}
