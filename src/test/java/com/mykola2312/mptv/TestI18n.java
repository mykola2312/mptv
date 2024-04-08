package com.mykola2312.mptv;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestI18n {
    @Test()
    public void testKeys() {
        assertEquals("Value1", I18n.get("Key1"));
        assertEquals("Value2", I18n.get("Key2"));
    }

    @Test()
    public void testNotFound() {
        assertEquals("KeyNotFound", I18n.get("KeyNotFound"));
    }
}
