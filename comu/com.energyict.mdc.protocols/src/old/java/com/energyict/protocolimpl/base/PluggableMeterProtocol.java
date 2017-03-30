/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

public abstract class PluggableMeterProtocol implements MeterProtocol {

    private final PropertySpecService propertySpecService;

    protected PluggableMeterProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public void addProperties(TypedProperties properties) {
        try {
            setProperties(properties.toStringProperties());
        } catch (InvalidPropertyException | MissingPropertyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getVersion() {
        return getProtocolVersion();
    }

}