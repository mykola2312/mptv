package com.mykola2312.mptv.mpv;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class MPVCommand {
    private int requestId;

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    protected abstract List<String> serializeCommand();

    public byte[] serialize() throws JsonProcessingException {
        MPVCommandRaw command = new MPVCommandRaw();
        command.request_id = requestId;
        command.command = serializeCommand();

        ObjectMapper mapper = new ObjectMapper();
        String jsonCommand = mapper.writeValueAsString(command) + "\n";
        return jsonCommand.getBytes(StandardCharsets.UTF_8);
    }
}
