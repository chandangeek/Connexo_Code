package com.elster.jupiter.rest.util.properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Not a real adapter. Serialization is delegated to JAckson anyway, but deserialization is skipped.
 */
public class PropertyTypeAdapter extends XmlAdapter<String, PropertyType> {

    @Override
    public PropertyType unmarshal(String v) throws Exception {
        return null; // we don't deserialize
    }

    @Override
    public String marshal(PropertyType v) throws Exception {
        return v.toString();
    }
}