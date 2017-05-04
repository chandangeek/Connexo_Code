/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class Codice extends AbstractField<Codice> {

    private int codice;

    public Codice() {
        this(0);
    }

    public Codice(int coda) {
        this.codice = coda;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(codice, getLength());
    }

    public Codice parse(byte[] rawData, int offset) throws CTRParsingException {
        this.codice = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getCodice() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice = codice;
    }
}
