package com.elster.jupiter.properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides mapping services for the JAXB marshalling mechanism
 * for the {@link Object} component.
 *
 * @author sva
 * @since 15/05/2014 - 10:48
 */
public class ObjectXmlMarshallAdapter extends XmlAdapter<ObjectXmlAdaptation, Object> {

    @Override
    public Object unmarshal (ObjectXmlAdaptation v) throws Exception {
        return v.unmarshallObject();
    }

    @Override
    public ObjectXmlAdaptation marshal (Object v) throws Exception {
        return new ObjectXmlAdaptation(v);
    }

}
