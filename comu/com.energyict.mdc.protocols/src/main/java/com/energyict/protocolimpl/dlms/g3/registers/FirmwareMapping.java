package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 9:48
 */
public class FirmwareMapping extends G3Mapping {

    public FirmwareMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        return new RegisterValue(getObisCode(), getFirmwareVersion(as330D));
    }

    /**
     * Read and cache the requested firmware version
     *
     * @return The firmware version as string
     * @throws java.io.IOException
     */
    private final String getFirmwareVersion(final AS330D as330D) throws IOException {
        int eField = getObisCode().getE();
        if (eField == 0) {
            return as330D.getFirmwareVersion();
        } else {
            String[] fwVersionEntries = as330D.getFirmwareVersion().split(", ");
            if (eField > fwVersionEntries.length) {
                throw new NoSuchRegisterException(getObisCode().toString());
            } else {
                return fwVersionEntries[eField - 1];
            }
        }
    }
}