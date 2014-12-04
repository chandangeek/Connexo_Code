package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the StartDate field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class StartDate extends AbstractField<StartDate> {

    private byte[] date;

    public byte[] getBytes() {
        return date;
    }

    public byte[] getDate() {
        return date;
    }

    public int getLength() {
        return 4;
    }

    public StartDate parse(byte[] rawData, int offset) throws CTRParsingException {
        date = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return this;
    }

    public void setDate(byte[] date) {
        this.date = date.clone();
    }

}