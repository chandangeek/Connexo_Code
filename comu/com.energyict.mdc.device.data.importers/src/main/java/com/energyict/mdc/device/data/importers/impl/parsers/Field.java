package com.energyict.mdc.device.data.importers.impl.parsers;

import jdk.nashorn.internal.runtime.ParserException;

public interface Field<T> {
    void parse(String value, T data) throws ParserException;

    int index();
}