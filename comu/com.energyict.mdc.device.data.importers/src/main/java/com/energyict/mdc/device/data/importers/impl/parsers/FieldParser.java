/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.parsers;

import com.energyict.mdc.device.data.importers.impl.exceptions.ValueParserException;

public interface FieldParser<T> {

    T parse(String value) throws ValueParserException;
}