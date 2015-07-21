package com.energyict.mdc.device.data.importers.impl.parsers;


import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberParser implements FieldParser<Number> {

    public Number parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyNumberString(value);
    }

    private Number parseNonEmptyNumberString(String value) throws ValueParserException {
        try {
            return NumberFormat.getInstance(Locale.ENGLISH).parse(value);
        } catch (Exception e) {
            throw new ValueParserException(value, "123456");
        }
    }
}