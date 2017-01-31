/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.field.parser;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.FloatField;

/**
 * @author sva
 * @since 11/09/2014 - 14:54
 */
public class FloatFieldParser implements FieldParser {

    private final Unit unit;

    public FloatFieldParser(Unit unit) {
        this.unit = unit;
    }

    public FloatField parse(byte[] rawData, int offset) throws ParsingException {
        return new FloatField(unit).parse(rawData, offset);
    }
}