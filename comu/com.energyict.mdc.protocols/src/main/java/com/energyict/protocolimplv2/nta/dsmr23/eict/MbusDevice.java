package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessaging;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;

/**
 * @author sva
 * @since 29/11/13 - 9:59
 */
public class MbusDevice extends AbstractNtaMbusDevice {

    private Dsmr23MbusMessaging dsmr23MbusMessaging;

    @Inject
    public MbusDevice(PropertySpecService propertySpecService,
                      TopologyService topologyService,
                      Provider<InheritedAuthenticationDeviceAccessLevel> authenticationDeviceAccessLevelProvider,
                      Provider<InheritedEncryptionDeviceAccessLevel> encryptionDeviceAccessLevelProvider, WebRTUKP webRtuKp) {
        super(propertySpecService, topologyService,
                authenticationDeviceAccessLevelProvider, encryptionDeviceAccessLevelProvider, webRtuKp);
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
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3) Mbus Slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}
