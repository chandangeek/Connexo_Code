package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the NackAdditionalData field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 16:19:20
 */
public class NackAdditionalData extends AbstractField<NackAdditionalData> {

    private byte[] data;

    public NackAdditionalData() {
        data = new byte[getLength()];
    }

    public int getLength() {
        return 20;
    }

    public byte[] getBytes() {
        return data;
    }

    public NackAdditionalData parse(byte[] rawData, int offset) throws CTRParsingException {
        this.data = new byte[getLength()];
        System.arraycopy(rawData, offset, data, 0, getLength());
        return this;
    }
}
