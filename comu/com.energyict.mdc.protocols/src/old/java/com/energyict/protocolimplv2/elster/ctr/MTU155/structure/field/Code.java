/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class Code extends AbstractField<Code> {

    private byte[] code;

    public byte[] getBytes() {
        return code;
    }

    public byte[] getCode() {
        return code;
    }

    public int getLength() {
        return code.length;
    }

    public Code parse(byte[] rawData, int offset) throws CTRParsingException {
        // From offset point up to the end, rawData contains Code bytes
        code = ProtocolTools.getSubArray(rawData, offset, rawData.length);
        return this;
    }

    public void setCode(byte[] code) {
        this.code = code.clone();
    }
}