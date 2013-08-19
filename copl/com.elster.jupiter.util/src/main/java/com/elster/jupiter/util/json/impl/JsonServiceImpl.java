package com.elster.jupiter.util.json.impl;

import com.elster.jupiter.util.json.JsonParseException;
import com.elster.jupiter.util.json.JsonService;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

@Component(name = "com.elster.jupiter.util.json", service = {JsonService.class})
public class JsonServiceImpl implements JsonService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonParseException(e, json, clazz);
        }
    }

    @Override
    public <T> T deserialize(byte[] json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonParseException(e, new String(json), clazz);
        }
    }

    @Override
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
