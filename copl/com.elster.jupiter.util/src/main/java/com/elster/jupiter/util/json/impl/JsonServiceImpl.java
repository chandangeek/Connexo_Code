/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json.impl;

import com.elster.jupiter.util.json.JsonDeserializeException;
import com.elster.jupiter.util.json.JsonSerializeException;
import com.elster.jupiter.util.json.JsonService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;

import java.io.IOException;

@Component(name = "com.elster.jupiter.util.json", service = {JsonService.class})
public class JsonServiceImpl implements JsonService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public JsonServiceImpl() {
        // supporting serialization of Java 8 time classes
        objectMapper.registerModule(new JSR310Module());
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonDeserializeException(e, json, clazz);
        }
    }

    @Override
    public <T> T deserialize(byte[] json, Class<T> clazz) {
        try {
            PolymorphicTypeValidator ptv =
                    BasicPolymorphicTypeValidator.builder()
                            .allowIfBaseType(clazz)
                            .build();
            ObjectMapper mapper = JsonMapper.builder()
                    .polymorphicTypeValidator(ptv)
                    .build();
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new JsonDeserializeException(e, new String(json), clazz);
        }
    }

    @Override
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new JsonSerializeException(e, object);
        }
    }
}
