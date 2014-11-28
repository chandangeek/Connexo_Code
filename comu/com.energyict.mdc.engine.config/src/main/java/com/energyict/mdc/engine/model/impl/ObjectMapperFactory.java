package com.energyict.mdc.engine.model.impl;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Provides factory services for the jackson ObjectMapper class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (13:31)
 */
public final class ObjectMapperFactory {

    public static ObjectMapper newMapper () {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(introspector);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return mapper;
    }

    // Hide utility class constructor
    private ObjectMapperFactory() {}

}