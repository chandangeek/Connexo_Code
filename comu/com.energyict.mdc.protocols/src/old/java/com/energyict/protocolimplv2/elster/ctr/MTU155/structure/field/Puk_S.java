/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

import java.util.Random;

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
        this.puks = puks.clone();
    }

    public final void setPuks() {
        puks = new byte[getLength()];
        new Random().nextBytes(puks);
    }

}
