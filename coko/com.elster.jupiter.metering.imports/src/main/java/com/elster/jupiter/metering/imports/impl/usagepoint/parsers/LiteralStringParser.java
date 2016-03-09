package com.elster.jupiter.metering.imports.impl.usagepoint.parsers;

import com.elster.jupiter.metering.imports.impl.usagepoint.FieldParser;
import com.elster.jupiter.util.Checks;

public class LiteralStringParser implements FieldParser<String> {

    public LiteralStringParser() {
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    public String parse(String value) {
        return !Checks.is(value).emptyOrOnlyWhiteSpace() ? value.trim() : null;
    }
}