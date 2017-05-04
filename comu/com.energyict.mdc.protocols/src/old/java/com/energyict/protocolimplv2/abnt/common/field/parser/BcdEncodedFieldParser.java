/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.field.parser;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 11/09/2014 - 14:27
 */
public class BcdEncodedFieldParser implements FieldParser {

    public int length;

    public BcdEncodedFieldParser(int length) {
        this.length = length;
    }

    public BcdEncodedField parse(byte[] rawData, int offset) throws ParsingException {
        return new BcdEncodedField(length).parse(rawData, offset);
    }

}
