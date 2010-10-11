package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 10:07:16
 */
public class AckAdditionalData extends AbstractField<AckAdditionalData> {

    public static final int LENGTH = 24;

    public byte[] getBytes() {
        return new byte[LENGTH];
    }

    public AckAdditionalData parse(byte[] rawData, int offset) throws CTRParsingException {
        return this;
    }
}
