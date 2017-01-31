/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class AckAdditionalData extends AbstractField<AckAdditionalData> {

    private byte[] additionalData;

    public AckAdditionalData() {
        additionalData = new byte[getLength()];
    }

    public int getLength() {
        return 24;
    }

    public byte[] getBytes() {
        return additionalData;
    }

    public AckAdditionalData parse(byte[] rawData, int offset) throws CTRParsingException {
        additionalData = new byte[getLength()];
        System.arraycopy(rawData, offset, additionalData, 0, getLength());
        return this;
    }
}
