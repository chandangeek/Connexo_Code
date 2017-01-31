/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.util.TimeZone;
import java.util.logging.Logger;

public class Beacon3100G3RegisterMapper extends G3RegisterMapper {

    /**
     * G3 register mapping, used to read data from the meter as a register value
     */
    public Beacon3100G3RegisterMapper(CosemObjectFactory cosemObjectFactory, TimeZone deviceTimeZone, Logger logger) {
        super(cosemObjectFactory, deviceTimeZone, logger);
    }

    /**
     * Only the G3 PLC registers are used here
     */
    @Override
    protected void initializeMappings() {
        this.mappings.addAll(getPLCStatisticsMappings());
        this.mappings.addAll(getBeaconPushEventNotificationAttibutesMappings());
        this.mappings.addAll(getIPv4SetupMappings());
        this.mappings.addAll(getUsbSetupRegistering());
        this.mappings.addAll(getDisconnectControlRegistering());
        this.mappings.addAll(getGprsModemSetupRegistering());
        this.mappings.addAll(getPPPSetupRegistering());
    }
}