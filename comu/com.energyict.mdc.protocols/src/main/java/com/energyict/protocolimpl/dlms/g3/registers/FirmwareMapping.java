/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.protocolimpl.dlms.g3.AS330D;
import com.energyict.protocolimpl.dlms.g3.FirmwareVersion;

import java.io.IOException;
import java.util.Date;

public class FirmwareMapping extends G3Mapping {

    public FirmwareMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        return new RegisterValue(getObisCode(), parseFirmwareVersion(as330D.getFirmwareVersion()));
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        return null;    //Not used here, the method above is overridden
    }

    /**
     * This method is not used here locally (instead, as330D.getFirmwareVersion() is used)
     * However, it can still be of use to expose the parsing of the abstractDataType (array)
     * containing the firmware version information.
     */
    @Override
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        Array fwEntries = (Array) abstractDataType;
        final StringBuilder sb = new StringBuilder();
        String fwVersionEntry;
        for (int i = 0; i < fwEntries.nrOfDataTypes(); i++) {
            fwVersionEntry = FirmwareVersion.fromStructure(fwEntries.getDataType(i, Structure.class)).getDisplayString();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('[').append(fwVersionEntry).append(']');
        }
        return new RegisterValue(getObisCode(), parseFirmwareVersion(sb.toString()));
    }

    /**
     * Read and cache the requested firmware version
     *
     * @return The firmware version as string
     * @throws java.io.IOException
     */
    private String parseFirmwareVersion(String fullFirmwareVersion) throws IOException {
        int eField = getObisCode().getE();
        if (eField == 0) {
            return fullFirmwareVersion;
        } else {
            String[] fwVersionEntries = fullFirmwareVersion.split(", ");
            if (eField > fwVersionEntries.length) {
                throw new NoSuchRegisterException(getObisCode().toString());
            } else {
                return fwVersionEntries[eField - 1];
            }
        }
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}