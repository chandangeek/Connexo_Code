/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimpl.base.CRC16DNP;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

public class Crc extends AbstractField<Crc> {

    public static final int LENGTH = 2;

    private int crc;

    public byte[] getBytes() {
        return getBytesFromInt(crc, LENGTH);
    }

    public Crc parse(byte[] rawData, int offset) {
        crc = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

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
        crc = CRC16DNP.calcCRC(data);
        return this;
    }
}
