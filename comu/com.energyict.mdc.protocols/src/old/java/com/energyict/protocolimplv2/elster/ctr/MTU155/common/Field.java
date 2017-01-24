package com.energyict.protocolimplv2.elster.ctr.MTU155.common;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:48:46
 */
public interface Field<T extends Field> {

    byte[] getBytes();

    T parse(byte[] rawData, int offset) throws CTRParsingException;

    int getLength();

}
