/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 23/05/2014 - 14:17
 */
public class Crc extends AbstractField<Crc> {

    public static final int LENGTH = 2;

    private int crc;

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(crc, LENGTH);
    }

    @Override
    public Crc parse(byte[] rawData, int offset) throws ParsingException {
        crc = getIntFromBytesLE(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public Crc generateAndSetCrc(byte[] data) {
        crc = CRCGenerator.calcCRCDirect(data);
        return this;
    }
}