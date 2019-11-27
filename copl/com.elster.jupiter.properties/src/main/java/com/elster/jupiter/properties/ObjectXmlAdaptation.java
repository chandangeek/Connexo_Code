package com.elster.jupiter.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.annotation.XmlElement;

/**
 * Adapter class for {@link Object} to enable xml (un)marshalling.
 * <br></br>
 * <b>Warning:</b> The Object should already be JSON marshable, this adapter will only ensure
 * the correct demarshalling/conversion is applied (to prevent the standard Object demarshalling is used).
 *
 * @author sva
 * @since 15/05/2014 - 10:49
 */
public class ObjectXmlAdaptation {

    @XmlElement
    public Object object;

    @XmlElement
    public String objectClazz;

    /**
     * The ObjectMapper used for (de)marshalling
     */
    private ObjectMapper objectMapper;

    public ObjectXmlAdaptation() {
        super();
    }

    public ObjectXmlAdaptation(Object obj) {
        this();
        this.object = obj;
        this.objectClazz = obj.getClass().getName();
    }

    public Object unmarshallObject() throws ClassNotFoundException {
        return getObjectMapper().convertValue(object, getObjectClazz());
            // Convert the Object (of simple type) back to a complex Object of given type
            // e.g. convert a LinkedHashMap back to its proper object
    }

    private Class<? extends Object> getObjectClazz() throws ClassNotFoundException {
        return Class.forName(objectClazz);
    }

    private ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        }
        return this.objectMapper;
    }
}
