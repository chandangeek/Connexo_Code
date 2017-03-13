/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.parsers;


import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
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