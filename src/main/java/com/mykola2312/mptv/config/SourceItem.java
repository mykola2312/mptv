package com.mykola2312.mptv.config;

import org.checkerframework.checker.nullness.qual.*;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceItem {
    public enum SourceType {
        @JsonProperty("m3u")
        M3U,

        @JsonProperty("m3u-local")
        M3U_LOCAL;

        public String getSqlName() {
            switch (this) {
                case M3U: return "m3u";
                case M3U_LOCAL: return "m3u-local";
                default: throw new RuntimeException("no sql alias for " + this.toString());
            }
        }
    }

    @NonNull
    public SourceType type;

    @Nullable
    public String url;

    @Nullable
    public String path;
    
    @Nullable
    public String cookies;

    @NonNull
    public String rootCategory;
}
