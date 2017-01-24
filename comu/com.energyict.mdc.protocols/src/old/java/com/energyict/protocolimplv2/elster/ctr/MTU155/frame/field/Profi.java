package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

/**
 * Class for the profi field in a frame
 * Copyrights EnergyICT
 * Date: 30-sep-2010
 * Time: 9:29:10
 */
public class Profi extends AbstractField<Profi> {

    private int profi;

    public Profi() {
        this(0);
    }

    public Profi(int profi) {
        this.profi = profi;
    }

    public byte[] getBytes() {
        return getBytesFromInt(profi, getLength());
    }

    public Profi parse(byte[] rawData, int offset) {
        profi = getIntFromBytes(rawData, offset, getLength());
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

    public int getLength() {
        return 1;
    }
}
