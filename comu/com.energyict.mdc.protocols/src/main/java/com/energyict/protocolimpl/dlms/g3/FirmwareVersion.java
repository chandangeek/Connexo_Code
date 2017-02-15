/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;

import java.io.IOException;

public class FirmwareVersion {

    private static final int REQUIRED_NR_FIELDS = 5;

    private final String firmwareId;
    private final boolean legallyRelevant;
    private final int majorVersion;
    private final int minorVersion;
    private final long checkSum;

    private FirmwareVersion(String firmwareId, boolean legallyRelevant, int majorVersion, int minorVersion, long checkSum) {
        this.firmwareId = firmwareId;
        this.legallyRelevant = legallyRelevant;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.checkSum = checkSum;
    }

    public String getFirmwareId() {
        return firmwareId;
    }

    public boolean isLegallyRelevant() {
        return legallyRelevant;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public long getCheckSum() {
        return checkSum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FirmwareVersion");
        sb.append("{firmwareId=").append(firmwareId);
        sb.append(", legallyRelevant=").append(legallyRelevant);
        sb.append(", majorVersion=").append(majorVersion);
        sb.append(", minorVersion=").append(minorVersion);
        sb.append(", checkSum=").append(checkSum);
        sb.append('}');
        return sb.toString();
    }

    public static FirmwareVersion fromStructure(Structure structure) throws IOException {
        if (structure == null) {
            throw new IOException("Unable to create FirmwareVersion object from 'null' structure.");
        } else if (structure.nrOfDataTypes() != REQUIRED_NR_FIELDS) {
            throw new IOException("Invalid firmware structure. Expected [" + REQUIRED_NR_FIELDS + "] elements but found [" + structure.nrOfDataTypes() + "]");
        }

        int index = 0;
        final String firmwareId = structure.getDataType(index++, OctetString.class).stringValue();
        final boolean legallyRelevant = structure.getDataType(index++, TypeEnum.class).getValue() == 0;
        final int majorVersion = structure.getDataType(index++, Unsigned8.class).getValue();
        final int minorVersion = structure.getDataType(index++, Unsigned8.class).getValue();
        final long checkSum = structure.getDataType(index++, Unsigned32.class).getValue();

        return new FirmwareVersion(firmwareId, legallyRelevant, majorVersion, minorVersion, checkSum);
    }

    public final String getDisplayString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(firmwareId);
        sb.append('-');
        sb.append(majorVersion);
        sb.append('.');
        sb.append(minorVersion);
        sb.append('-');
        sb.append(checkSum);
        return sb.toString();
    }
}
