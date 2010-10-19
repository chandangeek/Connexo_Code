package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;import java.util.*;

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

    public ReferenceDate parse(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setLenient(true);
        calendar.setTime(date);

        this.date = new byte[3];
        this.date[0] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        this.date[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
        this.date[2] = (byte) calendar.get(Calendar.DATE);

        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    public void setDate(byte[] date) {
        this.date = date;
    }

    public void setTomorrow() throws CTRParsingException {
        date[2] = (byte) (date[2] + 1);
        parse(date, 0);
    }

}