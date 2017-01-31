/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;


import com.elster.jupiter.metering.imports.impl.exceptions.ValueParserException;

public interface FieldParser<T> {

    Class<T> getValueType();

    T parse(String value) throws ValueParserException;
}