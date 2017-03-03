/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;


import com.elster.jupiter.fileimport.csvimport.FieldParser;

import static com.elster.jupiter.util.Checks.is;

public class LiteralStringParser implements FieldParser<String> {

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    public String parse(String value) {
        return !is(value).emptyOrOnlyWhiteSpace() ? value.trim() : null;
    }

}