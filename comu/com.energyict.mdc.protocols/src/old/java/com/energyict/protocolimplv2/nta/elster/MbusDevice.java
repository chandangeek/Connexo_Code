package com.energyict.protocolimplv2.nta.elster;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessaging;
import com.energyict.protocolimplv2.securitysupport.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.securitysupport.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * The MBus device used for the AM100 implementation of the NTA spec.
 *
 * @author sva
 * @since 2/11/12 (11:26)
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private Dsmr23MbusMessaging dsmr23MbusMessaging;

    @Inject
    public MbusDevice(Thesaurus thesaurus, PropertySpecService propertySpecService,
                      TopologyService topologyService,
                      Provider<InheritedAuthenticationDeviceAccessLevel> authenticationDeviceAccessLevelProvider,
                      Provider<InheritedEncryptionDeviceAccessLevel> encryptionDeviceAccessLevelProvider, WebRTUKP webRtuKp) {
        super(thesaurus, propertySpecService, topologyService, authenticationDeviceAccessLevelProvider, encryptionDeviceAccessLevelProvider, webRtuKp);
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        if (dsmr23MbusMessaging == null) {
            dsmr23MbusMessaging = new Dsmr23MbusMessaging(this, this.getTopologyService());
        }
        return dsmr23MbusMessaging;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 DLMS (PRE-NTA) Mbus Slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}