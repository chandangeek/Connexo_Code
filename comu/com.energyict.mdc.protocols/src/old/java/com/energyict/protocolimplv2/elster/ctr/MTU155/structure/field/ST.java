/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

public class ST extends AbstractField<ST> {

    public static final int UNDEFINED = 0;
    public static final int REMOTE_CLIENT = 1;
    public static final int TERMINAL = 2;

    private int st;

    public ST() {
        this(0);
    }

    public ST(int st) {
        this.st = st;
    }

    public int getLength() {
        return 1;
    }

    public byte[] getBytes() {
        return getBytesFromInt(st, getLength());
    }

    public ST parse(byte[] rawData, int offset) {
        this.st = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }
    
}
