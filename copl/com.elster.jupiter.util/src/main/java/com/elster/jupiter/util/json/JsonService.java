package com.elster.jupiter.util.json;

public interface JsonService {

    String serialize(Object oject);

    <T> T deserialize(String json, Class<T> clazz);

    <T> T deserialize(byte[] payload, Class<T> clazz);
}
