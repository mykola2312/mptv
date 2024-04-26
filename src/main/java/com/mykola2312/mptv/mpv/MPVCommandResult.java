package com.mykola2312.mptv.mpv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MPVCommandResult {
    public int request_id;
    public String error;
    public String data;

    public static MPVCommandResult deserialize(String data) throws JsonProcessingException {
        return new ObjectMapper()
            .readerFor(MPVCommandResult.class)
            .readValue(data);
    }
}
