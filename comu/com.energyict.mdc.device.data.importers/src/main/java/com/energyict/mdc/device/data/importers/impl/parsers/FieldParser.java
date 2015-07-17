package com.energyict.mdc.device.data.importers.impl.parsers;

import jdk.nashorn.internal.runtime.ParserException;

public interface FieldParser<T> {

    T parse(String value) throws ParserException;
}