/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;

import static com.elster.jupiter.util.Checks.is;

public class BooleanParser implements FieldParser<Boolean> {

    public Boolean parse(String value) throws ValueParserException {
        return !(is(value).emptyOrOnlyWhiteSpace() || !Boolean.TRUE.toString().equalsIgnoreCase(value));
    }

}