/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class LoadProfileReadSizeArgument extends AbstractField<LoadProfileReadSizeArgument> {

    public static final int LENGTH = 1;

    private int readSizeArgument;

    public LoadProfileReadSizeArgument() {
    }

    public LoadProfileReadSizeArgument(int readSizeArgument) {
        this.readSizeArgument = readSizeArgument;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(readSizeArgument, LENGTH);
    }

    @Override
    public LoadProfileReadSizeArgument parse(byte[] rawData, int offset) throws ParsingException {
        readSizeArgument = (char) getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getReadSizeArgument() {
        return readSizeArgument;
    }

    public void setReadSizeArgument(int readSizeArgument) {
        this.readSizeArgument = readSizeArgument;
    }
}