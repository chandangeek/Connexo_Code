package com.elster.jupiter.metering.imports.impl.usagepoint.parsers;


import com.elster.jupiter.metering.imports.impl.usagepoint.FieldParser;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
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