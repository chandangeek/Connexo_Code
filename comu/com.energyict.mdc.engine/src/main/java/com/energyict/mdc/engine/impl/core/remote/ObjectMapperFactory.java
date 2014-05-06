package com.energyict.mdc.engine.impl.core.remote;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

/**
 * Provides factory services for the jackson ObjectMapper class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (13:31)
 */
public final class ObjectMapperFactory {

    public static ObjectMapper newMapper () {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().withAnnotationIntrospector(introspector));
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().with(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        return mapper;
    }

    // Hide utility class constructor
    private ObjectMapperFactory () {}

}