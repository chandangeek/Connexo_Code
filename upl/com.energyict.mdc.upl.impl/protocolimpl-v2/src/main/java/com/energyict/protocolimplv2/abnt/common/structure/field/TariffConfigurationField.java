package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class TariffConfigurationField extends AbstractField<TariffConfigurationField> {

    public static final int LENGTH = 1;

    private int bitMaskCode;

    public TariffConfigurationField() {
        bitMaskCode = 0;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(bitMaskCode, LENGTH);
    }

    @Override
    public TariffConfigurationField parse(byte[] rawData, int offset) throws ParsingException {
        bitMaskCode = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getBitMaskCode() {
        return bitMaskCode;
    }

    public boolean peakTariffEnabled() {
        return (bitMaskCode & 0x01) == 0x01;
    }

    public boolean offPeakTariffEnabled() {
        return (bitMaskCode & 0x02) == 0x02;
    }

    public boolean nightTariffEnabled() {
        return (bitMaskCode & 0x04) == 0x04;
    }
}