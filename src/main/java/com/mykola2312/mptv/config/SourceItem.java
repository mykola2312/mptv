package com.mykola2312.mptv.config;

import org.checkerframework.checker.nullness.qual.*;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceItem {
    public enum SourceType {
        @JsonProperty("m3u")
        M3U
    }

    @NonNull
    public SourceType type;
    public String url;
    
    @Nullable
    public String cookies;

    @Nullable
    public String singleCategory;
}
