package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:19:20
 */
public class NackAdditionalData extends AbstractField<NackAdditionalData> {

    public static final int LENGTH = 20;

    private byte[] data = new byte[LENGTH];

    public byte[] getBytes() {
        return data;
    }

    public NackAdditionalData parse(byte[] rawData, int offset) throws CTRParsingException {
        this.data = new byte[LENGTH];
        System.arraycopy(rawData, offset, data, 0, LENGTH);
        return this;
    }
}
