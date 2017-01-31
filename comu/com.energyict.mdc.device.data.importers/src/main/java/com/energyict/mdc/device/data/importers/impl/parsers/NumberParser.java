/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import static com.elster.jupiter.util.Checks.is;

public class NumberParser implements FieldParser<Number> {

    public Number parse(String value) throws ValueParserException {
        if (is(value).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return parseNonEmptyNumberString(value);
    }

    private Number parseNonEmptyNumberString(String value) throws ValueParserException {
        try {
            ParsePosition parsePosition = new ParsePosition(0);
            Number parsed = NumberFormat.getInstance(Locale.ENGLISH).parse(value, parsePosition);
            if (parsePosition.getIndex() < value.length()) {
                // The input string was only partially parsed
                String trailing = value.substring(parsePosition.getIndex());
                if (is(trailing).emptyOrOnlyWhiteSpace()) {
                    return parsed;
                } else {
                    throw new ValueParserException(value, "123456");
                }
            } else {
                return parsed;
            }
        } catch (Exception e) {
            throw new ValueParserException(value, "123456");
        }
    }

}