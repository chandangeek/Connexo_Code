package com.energyict.genericprotocolimpl.elster.ctr.structure.field;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

import java.util.Random;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 8:02:24
 */
public class Puk_S extends AbstractField<Puk_S> {

    private byte[] puks;

    public Puk_S() {
        setPuks();
    }

    public byte[] getBytes() {
        return puks;
    }

    public Puk_S parse(byte[] rawData, int offset) {
        System.arraycopy(rawData, offset, puks, 0, getLength());
        return this;
    }

    public int getLength() {
        return 8;
    }

    public byte[] getPuks() {
        return puks;
    }

    public void setPuks(byte[] puks) {
        this.puks = puks;
    }

    public final void setPuks() {
        puks = new byte[getLength()];
        new Random().nextBytes(puks);
    }

}
