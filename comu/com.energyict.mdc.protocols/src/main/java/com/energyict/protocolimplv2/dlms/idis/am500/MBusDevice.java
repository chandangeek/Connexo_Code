/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am500;


import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Provider;

public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol;
    private final IDISMBusMessaging idisMBusMessaging;

    protected MBusDevice(Thesaurus thesaurus, PropertySpecService propertySpecService, Provider<InheritedAuthenticationDeviceAccessLevel> inheritedAuthenticationDeviceAccessLevelProvider, Provider<InheritedEncryptionDeviceAccessLevel> inheritedEncryptionDeviceAccessLevelProvider, AbstractDlmsProtocol masterProtocol) {
        super(thesaurus, propertySpecService, inheritedAuthenticationDeviceAccessLevelProvider, inheritedEncryptionDeviceAccessLevelProvider);
        this.masterProtocol = masterProtocol;
        this.idisMBusMessaging = new IDISMBusMessaging(masterProtocol);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM500 DLMS (IDIS P1) MBus slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-01-25 15:02:12 +0100 (Mon, 25 Jan 2016)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}