package com.energyict.protocolimplv2.abnt.common.field.parser;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 11/09/2014 - 14:23
 */
public interface FieldParser {

    public AbstractField parse(byte[] rawData, int offset) throws ParsingException;

}
