package com.mykola2312.mptv.mpv;

import java.util.Arrays;
import java.util.List;

public class MPVGetProperty extends MPVCommand {
    private final MPVProperty property;

    public MPVGetProperty(MPVProperty property) {
        this.property = property;
    }

    @Override
    protected List<String> serializeCommand() {
        return Arrays.asList("get_property", property.toString());
    }
}
