package com.energyict.mdc.device.data.importers.impl.parsers;

import com.elster.jupiter.util.Checks;

public class LiteralStringParser implements FieldParser<String> {

    public LiteralStringParser() {
    }

    public String parse(String value) {
        return !Checks.is(value).emptyOrOnlyWhiteSpace() ? value.trim() : null;
    }
}