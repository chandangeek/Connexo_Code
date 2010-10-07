package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

import java.util.Random;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 8:02:24
 */
public class Puk_S extends AbstractField<Puk_S> {

    public static final int LENGTH = 8;

    private byte[] puks;

    public Puk_S() {
        setPuks();
    }

    public byte[] getBytes() {
        return puks;
    }

    public Puk_S parse(byte[] rawData, int offset) {
        System.arraycopy(rawData, offset, puks, 0, LENGTH);
        return this;
    }

    public byte[] getPuks() {
        return puks;
    }

    public void setPuks(byte[] puks) {
        this.puks = puks;
    }

    public final void setPuks() {
        puks = new byte[LENGTH];
        new Random().nextBytes(puks);
    }

}
