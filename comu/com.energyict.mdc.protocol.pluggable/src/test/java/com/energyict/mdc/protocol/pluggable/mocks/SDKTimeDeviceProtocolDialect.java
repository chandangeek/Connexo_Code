/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import java.util.Optional;

public class SDKTimeDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final String clockOffsetToWritePropertyName = "ClockOffsetWhenReading";
    public static final String clockOffsetToReadPropertyName = "ClockOffsetWhenWriting";

    private final PropertySpecService propertySpecService;

    public SDKTimeDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKTimeDialect";
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect for time testing";
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new SDKTimeDialectCustomPropertySet(this.propertySpecService));
    }

}