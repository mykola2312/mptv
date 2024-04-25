package com.mykola2312.mptv.mpv;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MPVCommandRaw {
    public ArrayList<String> command;

    public MPVCommandRaw(String name, String... args) {
        command = new ArrayList<>();
        command.add(name);
        command.addAll(Arrays.asList(args));
    }

    public byte[] serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonCommand = mapper.writeValueAsString(this);
        return jsonCommand.getBytes(StandardCharsets.UTF_8);
    }
}
