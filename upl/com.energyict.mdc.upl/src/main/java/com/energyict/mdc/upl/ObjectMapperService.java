package com.energyict.mdc.upl;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Provides services for mapping objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-05 (16:18)
 */
public interface ObjectMapperService {
    /**
     * Getter for a new {@link ObjectMapper} that is configured with specific
     * {@link org.codehaus.jackson.map.AnnotationIntrospector}s
     * for both serialization and deserialization.
     * Following introspectors are set:
     * <ul>
     * <li>for serialization, a {@link org.codehaus.jackson.xc.JaxbAnnotationIntrospector} is set,
     * thus serialization will be done according to Jaxb annotations (e.g. @XmlAttribute, @XmlElement, ...);</li>
     * <li>for deserialization, a {@link org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector} is set,
     * thus deserialization is according to Jackson annotations (e.g. @JsonTypeInfo, which is useful for deserialization of polymorphic types);
     * </ul>
     *
     * @return The new ObjectMapper
     */
    ObjectMapper newJacksonMapper();
}