package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.registers;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/09/2015 - 9:16
 */
public class BeaconG3RegisterMapper extends G3RegisterMapper {

    /**
     * G3 register mapping, used to read data from the meter as a register value
     */
    public BeaconG3RegisterMapper(CosemObjectFactory cosemObjectFactory, TimeZone deviceTimeZone, Logger logger) {
        super(cosemObjectFactory, deviceTimeZone, logger);
    }

    /**
     * Only the G3 PLC registers are used here
     */
    @Override
    protected void initializeMappings() {
        this.mappings.addAll(getPLCStatisticsMappings());
    }
}