/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides factory services for the jackson ObjectMapper class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (13:31)
 */
public final class ObjectMapperFactory {

    private static ObjectMapper mapper = null;

    // Hide utility class constructor
    private ObjectMapperFactory() {
    }

    /**
     * Getter for a new {@link org.codehaus.jackson.map.ObjectMapper} who is configured with specific {@link org.codehaus.jackson.map.AnnotationIntrospector}s
     * for both serialization and deserialization. Following introspectors are set: <br></br>
     * <ul>
     * <li>for serialization, a {@link org.codehaus.jackson.xc.JaxbAnnotationIntrospector} is set, <br></br>
     * thus serialization will be done according to Jaxb annotations (e.g. @XmlAttribute, @XmlElement, ...);</li>
     * <li>for deserialization, a {@link org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector} is set, <br></br>
     * thus deserialization is according to Jackson annotations (e.g. @JsonTypeInfo, which is useful for deserialization of polymorphic types);
     * </ul>
     *
     * @return a new ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        if (mapper == null) {
            initializeMapper();
        }
        return mapper;
    }

    private static void initializeMapper() {
        mapper = new ObjectMapper();
        AnnotationIntrospector jacksonAnnotationIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector jaxbAnnotationIntrospector = new JaxbAnnotationIntrospector();
        AnnotationIntrospector introspector = AnnotationIntrospector.pair(jacksonAnnotationIntrospector, jaxbAnnotationIntrospector);

        // Serialization config
        mapper.setAnnotationIntrospector(jaxbAnnotationIntrospector);
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Deserialization config
        mapper.setAnnotationIntrospector(introspector);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        // Registered modules
        SimpleModule typedPropertiesDeserializerModule = new SimpleModule("TypedPropertiesDeserializerModule", new Version(1, 0, 0, null));
        mapper.registerModule(
                typedPropertiesDeserializerModule
                        .addDeserializer(TypedProperties.class, new TypedPropertiesJsonDeserializer(mapper)));
    }

    private static class TypedPropertiesJsonDeserializer extends JsonDeserializer<TypedProperties> {

        private final ObjectMapper mapper;

        private TypedPropertiesJsonDeserializer(ObjectMapper mapper) {
            this.mapper = mapper;
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
                    typedProperties = com.energyict.protocolimpl.properties.TypedProperties.inheritingFrom(this.createTypedPropertiesFor(inheritedProperties)); // Recursively parse the inherited TypedProperties
                } else {
                    typedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
                }

                if (typedPropertiesHashMap.containsKey("hashTable")) {
                    Map propertyMap = (LinkedHashMap) typedPropertiesHashMap.get("hashTable");
                    Map propertyClassMap = (LinkedHashMap) typedPropertiesHashMap.get("propertyKeyPropertyClassMap");
                    for (Object o : propertyMap.entrySet()) {
                        Map.Entry pairs = (Map.Entry) o;
                        String key = (String) pairs.getKey();
                        Object value = pairs.getValue();    // Value is either of simple type (String, Integer, ...) or a LinkedHashMap (JSON variant of a more complex object)
                        // which should be converted back to a proper object
                        String xmlType = (String) propertyClassMap.get(key);
                        try {
                            Class clazz = Class.forName(xmlType);
                            value = this.mapper.convertValue(value, clazz);
                        } catch (ClassNotFoundException | NullPointerException e) {
                            throw new JsonMappingException("Failed to unmarshall one or more of the property values of the TypedProperties");
                        }
                        typedProperties.setProperty(key, value);
                    }
                }
                return typedProperties;
            } else {
                return com.energyict.protocolimpl.properties.TypedProperties.empty();
            }
        }
    }

}