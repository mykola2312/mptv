package com.mykola2312.mptv.mpv;

import java.util.Arrays;
import java.util.List;

public class MPVSetProperty extends MPVCommand {
    private final MPVProperty property;
    private final String value;

    public MPVSetProperty(MPVProperty property, String value) {
        this.property = property;
        this.value = value;
    }

    public MPVSetProperty(MPVProperty property, int value) {
        this.property = property;
        this.value = String.valueOf(value);
    }

    public MPVSetProperty(MPVProperty property, float value) {
        this.property = property;
        this.value = String.valueOf(value);
    }

    @Override
    protected List<String> serializeCommand() {
        return Arrays.asList("set_property", property.toString(), value);
    }
}
