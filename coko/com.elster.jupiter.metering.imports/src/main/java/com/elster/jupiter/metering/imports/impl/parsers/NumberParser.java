/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.parsers;


import com.elster.jupiter.metering.imports.impl.FieldParser;
import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;
import com.elster.jupiter.util.Checks;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberParser implements FieldParser<Number> {

    private NumberFormat numberFormat;

    public NumberParser(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    @Override
    public Class<Number> getValueType() {
        return Number.class;
    }

    public Number parse(String value) throws ValueParserException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyNumberString(value);
    }

    private Number parseNonEmptyNumberString(String value) throws ValueParserException {
        try {
            return numberFormat.parse(value);
        } catch (Exception e) {
            throw new ValueParserException(value, "123456");
        }
    }
}