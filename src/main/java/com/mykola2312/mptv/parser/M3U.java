package com.mykola2312.mptv.parser;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.mykola2312.mptv.parser.M3UException.Type;

public class M3U {
    public class Item {
        @NonNull
        public String title;

        public String groupTitle;
        public String tvgLogo;

        @NonNull
        public String url;
    }

    public ArrayList<Item> items = new ArrayList<>();

    public M3U(ArrayList<Item> items) {
        this.items = items;
    }

    private static final Pattern HEADER = Pattern.compile("^#EXTM3U");
    private static final Pattern EXTINF = Pattern.compile("^(?:#EXTINF:-?\\d\\s*)(.*?),(.*)$");

    public static M3U parse(String data) throws M3UException {
        ArrayList<Item> items = new ArrayList<>();

        // state
        boolean headerLine = true;

        try (Scanner scanner = new Scanner(data)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (headerLine) {
                    Matcher matcher = HEADER.matcher(line);
                    if (!matcher.find()) {
                        throw new M3UException(Type.NOT_AN_M3U);
                    }

                    headerLine = false;
                } else {
                    Matcher matcher = EXTINF.matcher(line);
                    if (matcher.find()) {
                        String tags = matcher.group(1);
                        String title = matcher.group(2);

                        System.out.printf("tags: %s | title: %s\n", tags, title);
                    }
                }
            }
        }

        return new M3U(items);
    }
}
