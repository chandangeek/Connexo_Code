/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am540;


import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM540 E-meter (master) protocol.
 *
 * @author sva
 * @since 11/08/2015 - 14:49
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol;
    private final IDISMBusMessaging idisMBusMessaging;

    @Inject
    public MBusDevice(Thesaurus thesaurus, PropertySpecService propertySpecService, Provider<InheritedAuthenticationDeviceAccessLevel> inheritedAuthenticationDeviceAccessLevelProvider, Provider<InheritedEncryptionDeviceAccessLevel> inheritedEncryptionDeviceAccessLevelProvider, AbstractDlmsProtocol abstractDlmsProtocol) {
        super(thesaurus, propertySpecService, inheritedAuthenticationDeviceAccessLevelProvider, inheritedEncryptionDeviceAccessLevelProvider);
        this.masterProtocol = abstractDlmsProtocol;
        this.idisMBusMessaging = new IDISMBusMessaging(masterProtocol);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (IDIS P2) MBus slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-08-14 11:10:35 +0200 (Fri, 14 Aug 2015) $";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}
