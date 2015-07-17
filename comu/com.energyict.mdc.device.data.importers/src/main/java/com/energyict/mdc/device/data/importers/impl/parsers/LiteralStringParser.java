package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.parsers.FieldParser;

public class LiteralStringParser implements FieldParser<String> {

    public LiteralStringParser() {
    }

    public String parse(String value) {
        return value.trim();
    }
}