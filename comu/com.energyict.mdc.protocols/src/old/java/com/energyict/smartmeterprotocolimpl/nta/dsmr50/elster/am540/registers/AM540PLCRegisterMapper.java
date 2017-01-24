package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.registers;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.io.IOException;

/**
 * Mapper for the G3 PLC registers, reusing the functionality of the G3 protocol
 */
public class AM540PLCRegisterMapper extends G3RegisterMapper {

    private final DlmsSession dlmsSession;

    public AM540PLCRegisterMapper(DlmsSession dlmsSession) {
        super();
        this.dlmsSession = dlmsSession;
    }

    @Override
    protected void initializeMappings() {
        this.mappings.addAll(getPLCStatisticsMappings());
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        for (G3Mapping mapping : mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                final RegisterValue registerValue = mapping.readRegister(dlmsSession.getCosemObjectFactory());
                if (registerValue != null) {
                    return registerValue;
                }
            }
        }
        throw new NoSuchRegisterException(obisCode.toString());
    }
}