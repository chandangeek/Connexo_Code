package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:57
 */
public class ConcentratorFirmwareVersion extends AbstractField<ConcentratorFirmwareVersion> {

    public static final int LENGTH = 3;

    private String firmwareVersion;

    public ConcentratorFirmwareVersion() {
        this.firmwareVersion = new String();
    }

    public ConcentratorFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    @Override
    public byte[] getBytes() {
        return getBCDFromHexString(firmwareVersion, LENGTH);
    }

    @Override
    public ConcentratorFirmwareVersion parse(byte[] rawData, int offset) throws ParsingException {
        firmwareVersion = getHexStringFromBCD(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
}