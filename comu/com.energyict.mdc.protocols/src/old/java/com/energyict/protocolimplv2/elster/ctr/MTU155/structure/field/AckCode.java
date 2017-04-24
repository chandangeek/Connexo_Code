/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class AckCode extends AbstractField<AckCode> {

    private int ackCode;

    public AckCode() {
        this(0);
    }

    public AckCode(int ackCode) {
        this.ackCode = ackCode;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(ackCode, getLength());
    }

    public AckCode parse(byte[] rawData, int offset) throws CTRParsingException {
        ackCode = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

}
