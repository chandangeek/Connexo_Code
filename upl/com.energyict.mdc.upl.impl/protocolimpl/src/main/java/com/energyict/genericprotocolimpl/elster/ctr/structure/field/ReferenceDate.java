package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class ReferenceDate extends AbstractField<ReferenceDate> {

    private byte[] date;
    private static final int LENGTH = 3;

    public byte[] getBytes() {
        return date;
    }

    public byte[] getDate() {
        return date;
    }

    public ReferenceDate parse(byte[] rawData, int offset) throws CTRParsingException {
        date = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    public void setDate(byte[] date) {
        this.date = date;
    }

}