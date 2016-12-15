package com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;
import com.energyict.protocolimpl.dlms.g3.registers.MultiAPNConfigMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/09/2015 - 9:16
 */
public class Beacon3100G3RegisterMapper extends G3RegisterMapper {

    public static final ObisCode MULTI_APN_CONFIG = ObisCode.fromString("0.128.25.3.0.255");

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
        this.mappings.addAll(getMetrologyRegistering());
        this.mappings.addAll(getMultiAPNConfigurationMappings());
    }

    private final List<G3Mapping> getMultiAPNConfigurationMappings() {
        final List<G3Mapping> apnConfigsMappings = new ArrayList<G3Mapping>();
        apnConfigsMappings.add(new MultiAPNConfigMapping(MULTI_APN_CONFIG));
        return apnConfigsMappings;
    }
}