package com.mykola2312.mptv;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykola2312.mptv.crawler.SourceItem;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class TestConfig {
    public static final String JSON_CONFIG =
    """
        {
            "frame": {
              "width": 1366,
              "height": 1024,
              "fullscreen": false,
              "fontName": "Arial",
              "fontSize": 64
            },
          
            "db": {
              "url": "jdbc:sqlite:mptv.db",
              "user": "",
              "password": ""
            },
          
            "sources": [
              {
                "type": "m3u",
                "url": "https://example.com/list.m3u",
                "cookies": null,
                "rootCategory": "m3u"
              },
              {
                "type": "m3u-local",
                "path": "test.m3u8",
                "rootCategory": "m3u-local"
              }
            ]
          }
    """;

    private static Config loadConfig() throws IOException {
        return new ObjectMapper().readerFor(Config.class).readValue(JSON_CONFIG);
    }

    @Test()
    public void testConfigGeneral() {
        Config config = null;
        try {
            config = loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        assertNotNull(config);

        assertNotNull(config.frame);
        assertNotNull(config.db);
        assertNotNull(config.sources);
    }

    @Test()
    public void testSources() {
        Config config = null;
        try {
            config = loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        assertNotNull(config);
        assertNotNull(config.sources);

        assertNotEquals(0, config.sources.size());
        
        SourceItem m3u = config.sources.get(0);
        assertEquals(SourceItem.SourceType.M3U, m3u.type);
        assertEquals("https://example.com/list.m3u", m3u.url);
        assertEquals("m3u", m3u.rootCategory);
        assertNull(m3u.path);
        assertNull(m3u.cookies);

        SourceItem m3uLocal = config.sources.get(1);
        assertEquals(SourceItem.SourceType.M3U_LOCAL, m3uLocal.type);
        assertEquals("test.m3u8", m3uLocal.path);
        assertEquals("m3u-local", m3uLocal.rootCategory);
        assertNull(m3uLocal.url);
        assertNull(m3uLocal.cookies);
    }
}
