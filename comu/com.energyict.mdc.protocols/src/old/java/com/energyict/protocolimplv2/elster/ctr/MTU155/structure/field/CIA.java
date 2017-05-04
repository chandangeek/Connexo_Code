/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

import java.io.UnsupportedEncodingException;

public class CIA extends AbstractField<CIA> {

    private String cia;
    private static final int LENGTH = 5;

    public CIA(byte[] ciaBytes) {
        this.cia = ProtocolTools.getAsciiFromBytes(ciaBytes);
    }

    public CIA(String ciaString) {
        this.cia = ciaString;
    }

    public CIA() {
    }

    public String getCIA() {
        return cia;
    }

    public void setCIA(String cia) {
        this.cia = cia;
    }

    public byte[] getBytes() {
        try {
            byte[] ciaBytes = cia.getBytes("ASCII");
            if (ciaBytes.length < 5) {
                return ProtocolTools.concatByteArrays(new byte[5 - ciaBytes.length], ciaBytes);
            } else {
                return ProtocolTools.getSubArray(ciaBytes, 0, 5);
            }
        } catch (UnsupportedEncodingException e) {
            return new byte[5];
        }
    }

    public CIA parse(byte[] rawData, int offset) throws CTRParsingException {
        setCIA(ProtocolTools.getAsciiFromBytes(rawData));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }
}
