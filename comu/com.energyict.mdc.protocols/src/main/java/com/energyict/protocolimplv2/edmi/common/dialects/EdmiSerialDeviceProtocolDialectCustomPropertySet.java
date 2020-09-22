/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.common.dialects;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import com.energyict.protocolimplv2.edmi.dialects.ModemDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.SerialDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.UdpDeviceProtocolDialect;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link TcpDeviceProtocolDialect}, {@link UdpDeviceProtocolDialect}
 * and {@link ModemDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:44)
 */
public class EdmiSerialDeviceProtocolDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, EdmiDeviceProtocolDialectProperties> {

    @Inject
    public EdmiSerialDeviceProtocolDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, EdmiDeviceProtocolDialectProperties> getPersistenceSupport() {
        return new EdmiDeviceProtocolDialectPropertyPersistenceSupport();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SerialDeviceProtocolDialect(propertySpecService, getUplThesaurus());
    }
}