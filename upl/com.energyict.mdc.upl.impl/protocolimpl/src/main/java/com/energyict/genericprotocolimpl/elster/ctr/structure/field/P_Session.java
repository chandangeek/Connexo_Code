package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 11:18:42
 */
public class P_Session extends AbstractField<P_Session> {
    private int p_Session;
    private static final int LENGTH = 1;

    public P_Session(int p_Session) {
        this.p_Session = p_Session;
    }

    public P_Session() {
    }

    public int getP_Session() {
        return p_Session;
    }

    public void setP_Session(int p_Session) {
        this.p_Session = p_Session;
    }

    public byte[] getBytes() {
        return getBytesFromInt(getP_Session(), LENGTH);
    }

    public P_Session parse(byte[] rawData, int offset) throws CTRParsingException {
        setP_Session(getIntFromBytes(rawData, offset, LENGTH));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

}
