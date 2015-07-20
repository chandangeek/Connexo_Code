package com.energyict.mdc.device.data.importers.impl.parsers;

public class LiteralStringParser implements FieldParser<String> {

    public LiteralStringParser() {
    }

    public String parse(String value) {
        return value.trim();
    }
}