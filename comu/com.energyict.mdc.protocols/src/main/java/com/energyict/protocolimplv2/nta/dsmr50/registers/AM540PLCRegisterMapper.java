/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr50.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.io.IOException;
import java.util.List;
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
        this.getMappings().addAll(getPLCStatisticsMappings());
    }

    /**
     * Read out and return the G3 PLC register, or null if it's not supported in this mapper
     */
    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        List<G3Mapping> g3Mappings = getMappings();
        for (G3Mapping mapping : g3Mappings) {
            if (mapping.getObisCode().equals(obisCode)) {
                return mapping.readRegister(cosemObjectFactory);
            }
        }
        return null;
    }
}