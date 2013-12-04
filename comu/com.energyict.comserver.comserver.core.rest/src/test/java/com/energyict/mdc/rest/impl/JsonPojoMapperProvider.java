package com.energyict.mdc.rest.impl;

import javax.ws.rs.ext.ContextResolver;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

//@Provider
public class JsonPojoMapperProvider implements ContextResolver<ObjectMapper>{

    final ObjectMapper pojoMapper;

    public JsonPojoMapperProvider() {
        pojoMapper = new ObjectMapper();
        pojoMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return pojoMapper;
    }
}
