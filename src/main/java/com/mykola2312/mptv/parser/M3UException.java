package com.mykola2312.mptv.parser;

public class M3UException extends RuntimeException {
    public enum Type {
        NOT_AN_M3U
    };

    private static String typeToString(Type type) {
        switch (type) {
            case NOT_AN_M3U: return "not an m3u";
            default: return type.toString();
        }
    }

    public M3UException(Type type) {
        super(typeToString(type));
    }
}
