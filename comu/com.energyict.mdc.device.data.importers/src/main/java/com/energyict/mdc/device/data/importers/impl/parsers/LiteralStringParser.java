package com.energyict.mdc.device.data.importers.impl.parsers;

import static com.elster.jupiter.util.Checks.is;

public class LiteralStringParser implements FieldParser<String> {

    public String parse(String value) {
        return !is(value).emptyOrOnlyWhiteSpace() ? value.trim() : null;
    }

}