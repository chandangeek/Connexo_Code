package com.elster.jupiter.metering.imports.impl.usagepoint;


import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;

public interface FieldParser<T> {

    Class<T> getValueType();

    T parse(String value) throws ValueParserException;
}