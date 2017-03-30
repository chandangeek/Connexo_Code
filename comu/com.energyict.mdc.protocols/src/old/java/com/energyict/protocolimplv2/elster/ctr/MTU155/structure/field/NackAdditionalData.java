/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class NackAdditionalData extends AbstractField<NackAdditionalData> {

    private byte[] data;

    public NackAdditionalData() {
        data = new byte[getLength()];
    }

    public int getLength() {
        return 20;
    }

    public byte[] getBytes() {
        return data;
    }

    public NackAdditionalData parse(byte[] rawData, int offset) throws CTRParsingException {
        this.data = new byte[getLength()];
        System.arraycopy(rawData, offset, data, 0, getLength());
        return this;
    }
}
