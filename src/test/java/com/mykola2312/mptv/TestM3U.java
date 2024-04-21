package com.mykola2312.mptv;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.mykola2312.mptv.parser.M3U;
import com.mykola2312.mptv.parser.M3UException;
import com.mykola2312.mptv.parser.M3UParser;

public class TestM3U {
    private static final String M3U_GOOD = """
#EXTM3U
#EXTINF:-1 group-title="test1" tvg-logo="https://example.com/logo.jpg",Test title 1
http://example.org/video1.m3u8

#EXTINF:-1 group-title="test2" ,Test title 2
http://example.org/video2.m3u8
#EXTINF:-1 group-title="test3",Test title 3
#EXTVLCOPT:http-user-agent=User-Agent/1.0
http://example.org/video3.m3u8
            """;

    @Test
    public void testM3uGood() {
        ArrayList<M3U> items = M3UParser.parse(M3U_GOOD);
        assertNotEquals(0, items.size());

        M3U item = items.get(0);
        assertEquals("Test title 1", item.title);
        assertEquals("test1", item.groupTitle);
        assertEquals("http://example.org/video1.m3u8", item.url);
        assertEquals("https://example.com/logo.jpg", item.tvgLogo);

        item = items.get(1);
        assertEquals("Test title 2", item.title);
        assertEquals("test2", item.groupTitle);
        assertEquals("http://example.org/video2.m3u8", item.url);
        assertNull(item.tvgLogo);

        item = items.get(2);
        assertEquals("Test title 3", item.title);
        assertEquals("test3", item.groupTitle);
        assertEquals("http://example.org/video3.m3u8", item.url);
        assertNull(item.tvgLogo);
    }

    @Test
    public void testM3uBad() {
        assertThrows(M3UException.class, () -> M3UParser.parse("some random gibberish data"));

        ArrayList<M3U> items = M3UParser.parse("#EXTM3U");
        assertEquals(0, items.size());
    }
}
