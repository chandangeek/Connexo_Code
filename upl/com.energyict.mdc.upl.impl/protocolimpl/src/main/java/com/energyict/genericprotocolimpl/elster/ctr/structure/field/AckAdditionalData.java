package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Class for the AckAdditionalData field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 10:07:16
 */
public class AckAdditionalData extends AbstractField<AckAdditionalData> {

    private byte[] additionalData;

    public AckAdditionalData() {
        additionalData = new byte[getLength()];
    }

    public int getLength() {
        return 24;
    }

    public byte[] getBytes() {
        return additionalData;
    }

    public AckAdditionalData parse(byte[] rawData, int offset) throws CTRParsingException {
        additionalData = new byte[getLength()];
        System.arraycopy(rawData, offset, additionalData, 0, getLength());
        return this;
    }
}
