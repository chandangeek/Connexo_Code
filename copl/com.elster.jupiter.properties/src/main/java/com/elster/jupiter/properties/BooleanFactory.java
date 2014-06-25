package com.elster.jupiter.properties;

import static com.elster.jupiter.util.Checks.is;

public class BooleanFactory implements ValueFactory<Boolean> {

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    @Override
    public Boolean fromStringValue(String stringValue) {
        if (!is(stringValue).emptyOrOnlyWhiteSpace()
                && "1".equals(stringValue.trim())) {
            return Boolean.TRUE;
        }
        else {
            return Boolean.FALSE;
        }
    }

    @Override
    public String toStringValue(Boolean object) {
        if (Boolean.TRUE.equals(object)) {
            return "1";
        }
        else {
            return "0";
        }
    }
    
    @Override
    public boolean isReference() {
        return false;
    }
}