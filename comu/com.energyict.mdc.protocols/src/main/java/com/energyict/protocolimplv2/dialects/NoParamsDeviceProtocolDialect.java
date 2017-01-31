/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dialects;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;

import java.util.Optional;

public class NoParamsDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public NoParamsDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.NO_PARAMETERS_PROTOCOL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.NO_PARAMETERS_PROTOCOL).format();
    }

}