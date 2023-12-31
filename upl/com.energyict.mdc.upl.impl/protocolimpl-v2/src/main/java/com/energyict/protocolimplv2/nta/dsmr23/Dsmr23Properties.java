package com.energyict.protocolimplv2.nta.dsmr23;

import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;


/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class Dsmr23Properties extends DlmsProperties {

    public static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";



    public boolean getOldMbusDiscovery() {
        return parseBooleanProperty(OLD_MBUS_DISCOVERY, false);
    }

    public boolean replayAttackPreventionEnabled(){
       return getProperties().<Boolean>getTypedProperty(DlmsConfigurationSupport.REPLAY_ATTACK_PREVENTION, false);
    }

    //added while porting KaifaProperties from EIServer, since it seems it was forgotten when Dsmr23Properties was ported
    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, true);
    }
}
