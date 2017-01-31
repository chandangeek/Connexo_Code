/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import java.util.Optional;

/**
 * Models a DeviceProtocolDialect for a serial HDLC connection type (optical/RS485/... interface).
 * <p/>
 * Only difference is the default value for the server lower mac address: 16 instead of 0
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class EDPSerialDeviceProtocolDialect extends SerialDeviceProtocolDialect {

    public EDPSerialDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new EDPDeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

}