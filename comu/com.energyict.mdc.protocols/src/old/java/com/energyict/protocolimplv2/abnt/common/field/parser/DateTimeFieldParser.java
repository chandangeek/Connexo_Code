/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.field.parser;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;

import java.text.SimpleDateFormat;

/**
 * @author sva
 * @since 11/09/2014 - 14:20
 */
public class DateTimeFieldParser implements FieldParser {

    public int length;
    private SimpleDateFormat dateFormatter;

    public DateTimeFieldParser(SimpleDateFormat dateFormatter, int length) {
        this.length = length;
        this.dateFormatter = dateFormatter;
    }

    public DateTimeField parse(byte[] rawData, int offset) throws ParsingException {
        return new DateTimeField(dateFormatter, length).parse(rawData, offset);
    }
}