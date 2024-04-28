package com.mykola2312.mptv.piir;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PiIRDump {
    public String pre_data;
    public String data;

    public static PiIRDump deserialize(String data) throws JsonProcessingException {
        return new ObjectMapper()
            .readerFor(PiIRDump.class)
            .readValue(data);
    }
}
