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
public class Address extends AbstractField<Address> {

    public static final int LENGTH = 2;

    private int address;
    private boolean parseAsBigEndian = false;

    public Address() {
        this(false);
    }

    public Address(boolean parseAsBigEndian) {
        this.address = 0;
        this.parseAsBigEndian = parseAsBigEndian;
    }

    public Address(int address) {
        this(address, false);
    }

    public Address(int address, boolean parseAsBigEndian) {
        this.address = address;
        this.parseAsBigEndian = parseAsBigEndian;
    }

    @Override
    public byte[] getBytes() {
        return parseAsBigEndian
                ? getBytesFromInt(address, LENGTH)
                : getBytesFromIntLE(address, LENGTH);
    }

    @Override
    public Address parse(byte[] rawData, int offset) throws ParsingException {
        address = parseAsBigEndian
                ? getIntFromBytes(rawData, offset, LENGTH)
                : getIntFromBytesLE(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }
}