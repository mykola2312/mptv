package com.mykola2312.mptv.parser;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mykola2312.mptv.parser.M3UException.Type;

public class M3UParser {
    private static final Pattern HEADER = Pattern.compile("^#EXTM3U");
    private static final Pattern EXTINF = Pattern.compile("^(?:#EXTINF:-?\\d\\s*)(.*?),(.*)$");

    private enum ParserState {
        HEADER_LINE,
        EXTINF_LINE,
        URL_LINE
    }

    public static ArrayList<M3U> parse(String data) throws M3UException {
        ArrayList<M3U> items = new ArrayList<>();

        ParserState state = ParserState.HEADER_LINE;
        M3U item = null;

        try (Scanner scanner = new Scanner(data)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                switch (state) {
                    case HEADER_LINE -> {
                        Matcher matcher = HEADER.matcher(line);
                        if (!matcher.find()) {
                            throw new M3UException(Type.NOT_AN_M3U);
                        }

                        state = ParserState.EXTINF_LINE;
                    }
                    
                    case EXTINF_LINE -> {
                        Matcher matcher = EXTINF.matcher(line);
                        if (matcher.find()) {
                            String tags = matcher.group(1);
                            String title = matcher.group(2);

                            item = new M3U(tags, title);

                            state = ParserState.URL_LINE;
                        }
                    }

                    case URL_LINE -> {
                        String url = line;
                        
                        item.url = url;
                        items.add(item);

                        state = ParserState.EXTINF_LINE;
                    }
                }
            }
        }

        return items;
    }
}
