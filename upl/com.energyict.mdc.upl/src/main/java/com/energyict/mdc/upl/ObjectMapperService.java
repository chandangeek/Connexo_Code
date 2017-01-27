package com.energyict.mdc.upl;


import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides services for mapping objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-05 (16:18)
 */
public interface ObjectMapperService {
    /**
     * Getter for a new {@link ObjectMapper} that is configured with specific
     * {@link com.fasterxml.jackson.databind.AnnotationIntrospector}s
     * for both serialization and deserialization.
     * Following introspectors are set:
     * <ul>
     * <li>for serialization, a {@link com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector} is set,
     * thus serialization will be done according to Jaxb annotations (e.g. @XmlAttribute, @XmlElement, ...);</li>
     * <li>for deserialization, a {@link com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector} is set,
     * thus deserialization is according to Jackson annotations (e.g. @JsonTypeInfo, which is useful for deserialization of polymorphic types);
     * </ul>
     *
     * @return The new ObjectMapper
     */
    ObjectMapper newJacksonMapper();
}