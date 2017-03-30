/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.AesCMac128;

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
        AesCMac128 cmac = new AesCMac128(key.clone());
        return parse(cmac.getAesCMac128(data), 0);
    }

    /**
     * Get the value of the Cpa
     * @return
     */
    public int getCpa() {
        return cpa;
    }

    /**
     * Set the value of the Cpa
     * @param cpa
     */
    public void setCpa(int cpa) {
        this.cpa = cpa;
    }

    /**
     * Check if the cpa value is undefined (0x00000000)
     * This is the case for example in the Identification request
     * @return
     */
    public boolean isUndefined() {
        return cpa == 0;
    }

    @Override
    /**
     * Check if the Cpa equals another cpa or subclass
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cpa)) {
            return false;
        }

        Cpa cpa1 = (Cpa) o;

        if (cpa != cpa1.cpa) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return cpa;
    }

}
