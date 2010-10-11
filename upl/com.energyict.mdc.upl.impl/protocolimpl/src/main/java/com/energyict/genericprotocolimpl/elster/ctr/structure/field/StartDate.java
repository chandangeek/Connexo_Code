package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class StartDate extends AbstractField<StartDate> {

    public static final int LENGTH = 4;

    private byte[] date;
    private String sDate = "";

    public byte[] getBytes() {
        return date;
    }

    public byte[] getDate() {
        return date;
    }

    public StartDate parse(byte[] rawData, int offset) throws CTRParsingException {
        date = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        sDate = "";
        String prefix = "";
        for (byte byte1 : date) {
            sDate += prefix + Integer.toString(byte1 & 0xFF);
            prefix = ",";
        }
        return this;
    }

    public Date getDateObject() {
        byte[] b = getBytes();
        return new Date(b[0], b[1], b[2]);
    }

    public void setDate(byte[] date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return sDate;
    }
}