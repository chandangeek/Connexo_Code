package com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;

/**
 * Class for the address field in a frame
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 17:23:46
 */
public class Address extends AbstractField<Address> {

    private int address;
    public static final int LENGTH = 2;

    public Address() {
        this.address = 0;
    }

    public Address(int address) {
        this.address = address;
    }

    public byte[] getBytes() {
        return getBytesFromInt(address, LENGTH);
    }

    public Address parse(byte[] rawData, int offset) {
        address = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

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
