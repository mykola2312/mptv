package com.mykola2312.mptv.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MenuAction {
    @JsonProperty("up")
    ACTION_UP,

    @JsonProperty("down")
    ACTION_DOWN,

    @JsonProperty("left")
    ACTION_LEFT,
    
    @JsonProperty("right")
    ACTION_RIGHT,

    @JsonProperty("open")
    ACTION_OPEN
}
