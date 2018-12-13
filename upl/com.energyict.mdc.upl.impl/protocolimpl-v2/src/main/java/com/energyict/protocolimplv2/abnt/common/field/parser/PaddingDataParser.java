package com.energyict.protocolimplv2.abnt.common.field.parser;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.PaddingData;

/**
 * @author sva
 * @since 11/09/2014 - 14:27
 */
public class PaddingDataParser implements FieldParser {

    public int length;

    public PaddingDataParser() {
        this.length = 1;
    }

    public PaddingDataParser(int length) {
        this.length = length;
    }

    public PaddingData parse(byte[] rawData, int offset) throws ParsingException {
        return new PaddingData(length).parse(rawData, offset);
    }
}