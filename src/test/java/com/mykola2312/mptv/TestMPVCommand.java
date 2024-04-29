package com.mykola2312.mptv;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mykola2312.mptv.mpv.MPVCommand;
import com.mykola2312.mptv.mpv.MPVCommandResult;

public class TestMPVCommand {
    private class TestCommand extends MPVCommand {
        @Override
        protected List<String> serializeCommand() {
            return Arrays.asList("name", "arg1", "arg2");
        }

    }

    private static final byte[] EXPECTED_BUF =
        "{\"request_id\":0,\"command\":[\"name\",\"arg1\",\"arg2\"]}\n"
        .getBytes(StandardCharsets.UTF_8);

    @Test()
    public void testSerialize() throws JsonProcessingException {
        TestCommand testCommand = new TestCommand();

        assertDoesNotThrow(() -> testCommand.serialize());
        assertArrayEquals(EXPECTED_BUF, testCommand.serialize());
    }

    private static final String RESULT_LINE = "{\"data\":5.440000,\"request_id\":0,\"error\":\"success\"}\n";

    @Test()
    public void testDeserializeResult() throws JsonProcessingException {
        assertDoesNotThrow(() -> MPVCommandResult.deserialize(RESULT_LINE));
        MPVCommandResult result = MPVCommandResult.deserialize(RESULT_LINE);

        assertNotNull(result);
        assertNotNull(result.request_id);
        assertNotNull(result.error);
        assertEquals("5.440000", result.data);
    }
}
