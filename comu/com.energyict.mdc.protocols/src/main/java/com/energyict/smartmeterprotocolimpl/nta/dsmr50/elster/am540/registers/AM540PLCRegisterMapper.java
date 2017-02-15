/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Mapper for the G3 PLC registers, reusing the functionality of the G3 protocol
 */
public class AM540PLCRegisterMapper extends G3RegisterMapper {

    private final CosemObjectFactory cosemObjectFactory;

    public AM540PLCRegisterMapper(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, Logger logger) {
        super(cosemObjectFactory, timeZone, logger);
        this.cosemObjectFactory = cosemObjectFactory;
    }

    @Override
    protected void initializeMappings() {
        this.mappings.addAll(getPLCStatisticsMappings());
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        for (G3Mapping mapping : mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                final RegisterValue registerValue = mapping.readRegister(cosemObjectFactory);
                if (registerValue != null) {
                    return registerValue;
                }
            }
        }
        throw new NoSuchRegisterException(obisCode.toString());
    }
}