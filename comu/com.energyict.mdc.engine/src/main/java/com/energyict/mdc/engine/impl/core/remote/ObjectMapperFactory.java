/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.impl.ObjectMapperServiceImpl;
import com.energyict.mdc.engine.impl.OnlineJSONTypeMapper;
import com.energyict.mdc.upl.TypedProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import sun.util.calendar.ZoneInfo;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides factory services for the jackson ObjectMapper class.
 *
 * @author Rudi Vankeirsbilck (rudi)root
 * @since 2013-04-29 (13:31)
 */
public final class ObjectMapperFactory {

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper != null)
            return objectMapper;
        objectMapper = new ObjectMapper();

        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new GuavaModule());
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule customModule = new SimpleModule("TypedPropertiesDeserializerModule", new Version(1, 0, 0, null));
        customModule.addDeserializer(ZoneInfo.class, new ZoneInfoJsonDeserializer());
        customModule.addDeserializer(TypedProperties.class, new TypedPropertiesJsonDeserializer(objectMapper, new OnlineJSONTypeMapper()));
        objectMapper.registerModule(customModule);

        AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        objectMapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        return objectMapper;
    }

    // Hide utility class constructor
    private ObjectMapperFactory () {}

    /**
     * Apparently, a serialized TimeZone object needs custom support for de-serialization, it is not supported natively by JSON.
     */
    private static class ZoneInfoJsonDeserializer extends JsonDeserializer<ZoneInfo> {
        @Override
        public ZoneInfo deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            String zoneInfo = (String) new UntypedObjectDeserializer().deserialize(jsonParser, deserializationContext);
            return (ZoneInfo) ZoneInfo.getTimeZone(zoneInfo);
        }
    }

    /**
     * Serialization / de-serialization for the TypeProperties.
     */
    private static class TypedPropertiesJsonDeserializer extends JsonDeserializer<TypedProperties> {

        private ObjectMapper mapper;
        private ObjectMapperServiceImpl.JSONTypeMapper jsonTypeMapper;

        private TypedPropertiesJsonDeserializer(ObjectMapper mapper, ObjectMapperServiceImpl.JSONTypeMapper jsonTypeMapper) {
            this.mapper = mapper;
            this.jsonTypeMapper = jsonTypeMapper;
        }

        @Override
        public TypedProperties deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            Map typedPropertiesHashMap = (LinkedHashMap) new UntypedObjectDeserializer().deserialize(jp, ctxt);
            return createTypedPropertiesFor(typedPropertiesHashMap);
        }

        /**
         * Create a proper {@link TypedProperties} object from the given LinkedHashMap,
         * which contains the JSON marshalled version of the TypedProperties object
         *
         * @param typedPropertiesHashMap the JSON marsahlled version of the TypedProperties object
         * @return a proper TypedProperties object (including its inherited TypedProperties)
         */
        private TypedProperties createTypedPropertiesFor(Map typedPropertiesHashMap) throws JsonMappingException {
            if (typedPropertiesHashMap != null) {
                TypedProperties typedProperties;
                if (typedPropertiesHashMap.containsKey("inheritedProperties")) {
                    Map inheritedProperties = (LinkedHashMap) typedPropertiesHashMap.get("inheritedProperties");
                    typedProperties = TypedProperties.inheritingFrom(this.createTypedPropertiesFor(inheritedProperties)); // Recursively parse the inherited TypedProperties
                } else {
                    typedProperties = TypedProperties.empty();
                }

                if (typedPropertiesHashMap.containsKey("hashTable") || typedPropertiesHashMap.containsKey("hashMap")) {
                    Map propertyMap = (LinkedHashMap) (typedPropertiesHashMap.get("hashTable") == null ? typedPropertiesHashMap.get("hashMap") : typedPropertiesHashMap.get("hashTable"));
                    Map propertyClassMap = (LinkedHashMap) typedPropertiesHashMap.get("propertyKeyPropertyClassMap");
                    for (Object o : propertyMap.entrySet()) {
                        Map.Entry pairs = (Map.Entry) o;
                        String key = (String) pairs.getKey();
                        Object value = pairs.getValue();    // Value is either of simple type (String, Integer, ...) or a LinkedHashMap (JSON variant of a more complex object)
                        // which should be converted back to a proper object
                        String xmlType = (String) propertyClassMap.get(key);
                        try {
                            Class clazz = this.jsonTypeMapper.classForName(xmlType);
                            value = this.mapper.convertValue(value, clazz);
                        } catch (ClassNotFoundException | NullPointerException e) {
                            throw new JsonMappingException("Failed to unmarshall one or more of the property values of the TypedProperties", e);
                        }
                        typedProperties.setProperty(key, value);
                    }
                }
                return typedProperties;
            } else {
                return TypedProperties.empty();
            }
        }
    }
}