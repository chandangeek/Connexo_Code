package com.elster.jupiter.metering.imports.impl.parsers;


import com.elster.jupiter.metering.imports.impl.FieldParser;
import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;
import com.elster.jupiter.util.Checks;

public class BooleanParser implements FieldParser<Boolean> {

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }

    public Boolean parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace() || !Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return false;
        }
        return true;
    }
}