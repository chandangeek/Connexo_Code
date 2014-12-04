package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class MeterSerialNumber extends AbstractField<MeterSerialNumber> {

    public static final int LENGTH = 4;

    private BcdEncodedField serialNumber;

    public MeterSerialNumber() {
        this.serialNumber = new BcdEncodedField(LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return serialNumber.getBytes();
    }

    @Override
    public MeterSerialNumber parse(byte[] rawData, int offset) throws ParsingException {
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

    public void setSerialNumber(BcdEncodedField serialNumber) {
        this.serialNumber = serialNumber;
    }
}