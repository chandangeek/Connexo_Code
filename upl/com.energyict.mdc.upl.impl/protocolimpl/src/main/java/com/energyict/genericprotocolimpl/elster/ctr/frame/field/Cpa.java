package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.AesCMac128;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class Cpa extends AbstractField<Cpa> {

    public static final int LENGTH = 4;

    private int cpa;

    public Cpa() {
        this(0);
    }

    public Cpa(int cpa) {
        this.cpa = cpa;
    }

    public byte[] getBytes() {
        return getBytesFromInt(cpa, LENGTH);
    }

    public Cpa parse(byte[] rawData, int offset) {
        cpa = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    public Cpa generateCpa(byte[] data, byte[] key) {
        AesCMac128 cmac = new AesCMac128(key);
        return parse(cmac.getAesCMac128(data), 0);
    }

    public int getCpa() {
        return cpa;
    }

    public void setCpa(int cpa) {
        this.cpa = cpa;
    }

}
