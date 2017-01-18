package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;
import com.energyict.protocolimplv2.securitysupport.DsmrSecuritySupport;
import com.energyict.protocolimplv2.securitysupport.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.securitysupport.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * <p/>
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM450 e-meter (master) protocol.
 *
 * @author sva
 * @since 23/01/2015 - 9:26
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final IDISMBusMessaging idisMBusMessaging;
    private final AbstractDlmsProtocol masterProtocol;

    @Inject
    public MBusDevice(PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService,
                      IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService,
                      IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService,
                      LoadProfileFactory loadProfileFactory, Clock clock, Thesaurus thesaurus, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider,
                      Provider<InheritedEncryptionDeviceAccessLevel> inheritedEncryptionDeviceAccessLevelProvider,
                      Provider<InheritedAuthenticationDeviceAccessLevel> inheritedAuthenticationDeviceAccessLevelProvider) {
        super(thesaurus, propertySpecService, inheritedAuthenticationDeviceAccessLevelProvider, inheritedEncryptionDeviceAccessLevelProvider);
        masterProtocol = new AM540(propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService,
                collectedDataFactory, meteringService, loadProfileFactory, clock, thesaurus, dsmrSecuritySupportProvider);
        idisMBusMessaging = new IDISMBusMessaging(masterProtocol);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (NTA DSMR5.0) MBus slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-04-29 16:11:31 +0200 (Wed, 29 Apr 2015) $";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }

}