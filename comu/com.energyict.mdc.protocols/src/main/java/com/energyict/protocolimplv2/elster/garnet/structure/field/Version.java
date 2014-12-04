package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class Version extends AbstractField<Version> {

    public static final int LENGTH = 1;

    private int version;

    public Version() {
        this.version = 0;
    }

    public Version(int version) {
        this.version = version;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(version, LENGTH);
    }

    @Override
    public Version parse(byte[] rawData, int offset) throws ParsingException {
        version = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getVersion() {
        return version;
    }

    public String getVersionInfo() {
        return version == 1
                ? "Slave that can execute commands via serial number and does respond the sets state"
                : "Slave that DO NOT execute commands via serial number and DO NOT respond the sets state";
    }

    public void setVersion(int version) {
        this.version = version;
    }
}