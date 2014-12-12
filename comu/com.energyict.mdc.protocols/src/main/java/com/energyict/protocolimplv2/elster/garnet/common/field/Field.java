package com.energyict.protocolimplv2.elster.garnet.common.field;

import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:12
 */
public interface Field<T extends Field> {

    byte[] getBytes();

    T parse(byte[] rawData, int offset) throws ParsingException;

    int getLength();

}