package com.elster.jupiter.properties;

public class StringFactory implements ValueFactory<String> {

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public String fromStringValue(String stringValue) {
        if (stringValue == null) {
            return "";
        }
        return stringValue;
    }

    @Override
    public String toStringValue(String object) {
        return object;
    }
    
    @Override
    public boolean isReference() {
        return false;
    }
}