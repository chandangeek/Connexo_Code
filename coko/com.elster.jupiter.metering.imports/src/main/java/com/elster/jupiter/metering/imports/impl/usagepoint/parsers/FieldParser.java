package com.elster.jupiter.metering.imports.impl.usagepoint.parsers;


import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;

public interface FieldParser<T> {

    T parse(String value) throws ValueParserException;
}