package com.energyict.genericprotocolimpl.elster.ctr.common;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:48:46
 */
public interface Field<T extends Field> {

    byte[] getBytes();

    T parse(byte[] rawData, int offset) throws CTRParsingException;

}
