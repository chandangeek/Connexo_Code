/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class ReaderSerialNumber extends AbstractField<ReaderSerialNumber> {

    public static final int LENGTH = 3;

    private BcdEncodedField serialNumber;

    public ReaderSerialNumber() {
        this.serialNumber = new BcdEncodedField(LENGTH);
    }

    public ReaderSerialNumber(int serialNumber) {
        this.serialNumber = new BcdEncodedField(LENGTH, Integer.toString(serialNumber));
    }

    @Override
    public byte[] getBytes() {
        return serialNumber.getBytes();
    }

    @Override
    public ReaderSerialNumber parse(byte[] rawData, int offset) throws ParsingException {
        serialNumber.parse(rawData, offset);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public BcdEncodedField getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = new BcdEncodedField(LENGTH, Integer.toString(serialNumber));
    }
}