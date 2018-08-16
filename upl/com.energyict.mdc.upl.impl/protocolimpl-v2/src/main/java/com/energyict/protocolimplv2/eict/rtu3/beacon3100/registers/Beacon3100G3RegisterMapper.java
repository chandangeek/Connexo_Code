package com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/09/2015 - 9:16
 */
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
        this.mappings.addAll(getNTPSetupMappings());
        this.mappings.addAll(getModemWatchdogMappings());
        this.mappings.addAll(getPLCStatisticsMappings());
        this.mappings.addAll(getBeaconPushEventNotificationAttributesMappings());
        this.mappings.addAll(getPushSetupMappings());
        this.mappings.addAll(getIPv4SetupMappings());
        this.mappings.addAll(getUsbSetupMappings());
        this.mappings.addAll(getGprsModemSetupMappings());
        this.mappings.addAll(getPPPSetupRegistering());
        this.mappings.addAll(getMultiAPNConfigurationMappings());
        this.mappings.addAll(getConcentratorSetupMappings());
        this.mappings.addAll(getMemoryManagementMappings());
        this.mappings.addAll(getLastFirmwareActivationMappings());
        this.mappings.addAll(getLimiterMappings());
        this.mappings.addAll(getSNMPSetupMappings());
        this.mappings.addAll(getLTEMonitoringMappings());
        this.mappings.addAll(getGSMDiagnosticsMappings());
        this.mappings.addAll(getG3PlcJoinRequestTimestampMapping());
        this.mappings.addAll(getWWANStateTransitionMappings());
    }

    // for debug purposes
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(G3Mapping mapping : mappings){
            sb.append(mapping.getBaseObisCode()).append(";");
            sb.append(mapping.getObisCode()).append(";");
            sb.append(mapping.getClass().getCanonicalName()).append("\r\n");
        }
        return sb.toString();
    }

}