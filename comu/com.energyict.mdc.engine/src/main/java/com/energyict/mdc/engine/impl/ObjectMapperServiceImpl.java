package com.energyict.mdc.engine.impl;

import com.energyict.mdc.upl.ObjectMapperService;
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
import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides an implementation for the {@link ObjectMapperService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (08:46)
 */
public class ObjectMapperServiceImpl implements ObjectMapperService {
    private final JSONTypeMapper jsonTypeMapper;

    public interface JSONTypeMapper {

        String TYPE_ATTRIBUTE = "type";

        Class classForName(String className) throws ClassNotFoundException;

        /**
         * Convert all class names, used in the given JSONObject/JSONArray, to their remote variant.
         * This operation is done in place; this method has void return type, but the given object will be modified
         *
         * @param objectJSON the JSON to correct class names for, which should be of type {@link org.json.JSONObject} or {@link org.json.JSONArray}
         * @throws JSONException
         * @throws ClassNotFoundException
         */
        void convertAllClassNamesFor(Object objectJSON) throws JSONException, ClassNotFoundException;
    }

    public ObjectMapperServiceImpl(JSONTypeMapper jsonTypeMapper) {
        this.jsonTypeMapper = jsonTypeMapper;
    }

    @Override
    public ObjectMapper newJacksonMapper() {
        ObjectMapper mapper = new ObjectMapper();
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
                        .addDeserializer(TypedProperties.class, new TypedPropertiesJsonDeserializer(mapper, this.jsonTypeMapper)));
        return mapper;
    }

    private static class TypedPropertiesJsonDeserializer extends JsonDeserializer<TypedProperties> {

        private final ObjectMapper mapper;
        private final JSONTypeMapper jsonTypeMapper;

        private TypedPropertiesJsonDeserializer(ObjectMapper mapper, JSONTypeMapper jsonTypeMapper) {
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
        private com.energyict.mdc.common.TypedProperties createTypedPropertiesFor(Map typedPropertiesHashMap) throws JsonMappingException {
            if (typedPropertiesHashMap != null) {
                com.energyict.mdc.common.TypedProperties typedProperties;
                if (typedPropertiesHashMap.containsKey("inheritedProperties")) {
                    Map inheritedProperties = (LinkedHashMap) typedPropertiesHashMap.get("inheritedProperties");
                    typedProperties = com.energyict.mdc.common.TypedProperties.inheritingFrom(this.createTypedPropertiesFor(inheritedProperties)); // Recursively parse the inherited TypedProperties
                } else {
                    typedProperties = com.energyict.mdc.common.TypedProperties.empty();
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
                            Class clazz = this.jsonTypeMapper.classForName(xmlType);
                            value = this.mapper.convertValue(value, clazz);
                        } catch (ClassNotFoundException | NullPointerException e) {
                            throw new JsonMappingException("Failed to unmarshall one or more of the property values of the TypedProperties");
                        }
                        typedProperties.setProperty(key, value);
                    }
                }
                return typedProperties;
            } else {
                return com.energyict.mdc.common.TypedProperties.empty();
            }
        }
    }

}