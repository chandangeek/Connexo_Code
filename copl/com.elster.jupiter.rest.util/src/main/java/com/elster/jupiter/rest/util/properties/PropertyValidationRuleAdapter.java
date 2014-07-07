package com.elster.jupiter.rest.util.properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Not a real adapter. Serialization is delegated to JAckson anyway, but deserialization is skipped.
 */
public class PropertyValidationRuleAdapter extends XmlAdapter<Object, PropertyValidationRule> {

    @Override
    public PropertyValidationRule unmarshal(Object v) throws Exception {
        return null; // we don't deserialize
    }

    @Override
    public Object marshal(PropertyValidationRule v) throws Exception {
        return v;
    }
}
