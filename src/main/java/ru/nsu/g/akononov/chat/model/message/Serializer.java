package ru.nsu.g.akononov.chat.model.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Serializer {
    public static byte[] serialize(Message message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsBytes(message);
    }

    public static Message deserialization(byte[] message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(message, Message.class);
    }
}
