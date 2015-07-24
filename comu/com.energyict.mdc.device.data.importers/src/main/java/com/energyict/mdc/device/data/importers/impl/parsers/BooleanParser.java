package com.energyict.mdc.device.data.importers.impl.parsers;


import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;

public class BooleanParser implements FieldParser<Boolean> {

    public Boolean parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace() || !Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return false;
        }
        return true;
    }
}