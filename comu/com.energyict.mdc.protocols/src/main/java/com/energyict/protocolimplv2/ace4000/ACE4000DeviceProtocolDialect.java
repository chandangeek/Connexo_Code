/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000;

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
 * Models a GPRS {@link DeviceProtocolDialect} for the {@link ACE4000Outbound} protocol.
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class ACE4000DeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public ACE4000DeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new ACE4000DeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.ACE4000_DEVICE_PROTOCOL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.ACE4000_DEVICE_PROTOCOL).format();
    }

}