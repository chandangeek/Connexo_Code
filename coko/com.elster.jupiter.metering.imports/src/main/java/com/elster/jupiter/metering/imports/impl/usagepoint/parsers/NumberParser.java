package com.elster.jupiter.metering.imports.impl.usagepoint.parsers;


import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
import com.elster.jupiter.util.Checks;

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