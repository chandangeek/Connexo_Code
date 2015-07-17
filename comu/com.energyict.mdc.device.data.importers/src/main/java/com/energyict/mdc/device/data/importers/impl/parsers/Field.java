package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ParserException;

public interface Field<T> {

    void parse(String value, T data) throws ParserException;

    int index();
}