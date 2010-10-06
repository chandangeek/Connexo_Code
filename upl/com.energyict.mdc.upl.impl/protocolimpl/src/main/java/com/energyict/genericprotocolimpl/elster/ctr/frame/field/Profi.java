package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

/**
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class Profi extends AbstractField<Profi> {

    public static final int LENGTH = 1;
    private int profi;

    public byte[] getBytes() {
        return getBytesFromInt(profi, LENGTH);
    }

    public Profi parse(byte[] rawData, int offset) {
        profi = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public boolean isLongFrame() {
        return (getProfi() & 0x080) != 0x00;
    }

    public void setLongFrame(boolean longFrame) {
        if (longFrame) {
            profi |= 0x080;
        } else {
            profi &= 0x07F;
        }
    }

    public int getProfi() {
        return profi;
    }

    public void setProfi(int profi) {
        this.profi = profi;
    }
}
