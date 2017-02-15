/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Optional;


/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type to a Beacon 3100, that acts as a tranparent gateway to a connected PLC G3 e-meter.
 * Note that, using this dialect, the protocol will read out the actual meter (using the Beacon as a gateway).
 *
 * @author: khe
 */
public class GatewayTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public GatewayTcpDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.BEACON_GATEWAY_TCP_DLMS_PROTOCOL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.BEACON_GATEWAY_TCP_DLMS_PROTOCOL).format();
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new GatewayTcpDeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }
}
