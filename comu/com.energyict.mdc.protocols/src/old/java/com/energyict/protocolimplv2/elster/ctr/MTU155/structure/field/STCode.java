/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

public class STCode extends AbstractField<STCode> {

    public static final int UNDEFINED = 0;
    public static final int REMOTE_CLIENT = 1;
    public static final int TERMINAL = 2;

    private int stCode;

    public STCode() {
        this(0);
    }

    public STCode(int stCode) {
        this.stCode = stCode;
    }

    public int getLength() {
        return 2;
    }

    public byte[] getBytes() {
        return getBytesFromInt(stCode, getLength());
    }

    public STCode parse(byte[] rawData, int offset) {
        this.stCode = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getStCode() {
        return stCode;
    }

    public void setStCode(int stCode) {
        this.stCode = stCode;
    }

}
