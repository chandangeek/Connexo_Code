package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the AckCode field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:45:26
 */
public class AckCode extends AbstractField<AckCode> {

    private int ackCode;

    public AckCode() {
        this(0);
    }

    public AckCode(int ackCode) {
        this.ackCode = ackCode;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(ackCode, getLength());
    }

    public AckCode parse(byte[] rawData, int offset) throws CTRParsingException {
        ackCode = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

}
