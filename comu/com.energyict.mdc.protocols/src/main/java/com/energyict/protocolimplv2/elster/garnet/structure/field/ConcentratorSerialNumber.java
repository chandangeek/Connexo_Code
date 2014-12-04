package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:57
 */
public class ConcentratorSerialNumber extends AbstractField<ConcentratorSerialNumber> {

    public static final int LENGTH = 6;

    private String serialNumber;

    public ConcentratorSerialNumber() {
        this.serialNumber = new String();
    }

    public ConcentratorSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public byte[] getBytes() {
        return getBCDFromHexString(serialNumber, LENGTH);
    }

    @Override
    public ConcentratorSerialNumber parse(byte[] rawData, int offset) throws ParsingException {
        serialNumber = getHexStringFromBCD(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}