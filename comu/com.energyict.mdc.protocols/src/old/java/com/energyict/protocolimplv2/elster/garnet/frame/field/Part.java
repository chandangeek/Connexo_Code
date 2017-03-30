/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.frame.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class Part extends AbstractField<Part> {

    public static final int LENGTH = 1;

    private int part;

    public Part() {
        this.part = 0;
    }

    public Part(int part) {
        this.part = part;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(part, LENGTH);
    }

    @Override
    public Part parse(byte[] rawData, int offset) throws ParsingException {
        part = getIntFromBytesLE(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }
}