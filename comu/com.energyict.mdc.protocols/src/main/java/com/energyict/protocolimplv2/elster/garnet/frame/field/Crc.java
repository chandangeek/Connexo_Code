package com.energyict.protocolimplv2.elster.garnet.frame.field;

import com.energyict.protocolimplv2.elster.garnet.common.CRCGenerator;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 14:17
 */
public class Crc extends AbstractField<Crc> {

    public static final int LENGTH = 2;

    private int crc;

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(crc, LENGTH);
    }

    @Override
    public Crc parse(byte[] rawData, int offset) throws ParsingException {
        crc = getIntFromBytesLE(rawData, offset, LENGTH);
        return this;
    }

    @Override
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
        return generateAndSetCrc(data, data.length);
    }

    public Crc generateAndSetCrc(byte[] data, int length) {
        crc = CRCGenerator.calcCRC16(data, length);
        return this;
    }
}