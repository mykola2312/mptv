package com.mykola2312.mptv.mpv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MPVCommandResult {
    public int request_id;
    public String error;

    // parse always as string to avoid headache with different types
    @JsonProperty("data")
    public String data;

    public static MPVCommandResult deserialize(String data) throws JsonProcessingException {
        return new ObjectMapper()
            .readerFor(MPVCommandResult.class)
            .readValue(data);
    }
}
