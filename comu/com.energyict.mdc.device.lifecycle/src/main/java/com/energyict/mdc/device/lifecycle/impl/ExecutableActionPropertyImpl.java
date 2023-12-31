/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

/**
 * Provides a general purpose implementation for the {@link ExecutableActionProperty} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-08 (13:25)
 */
public class ExecutableActionPropertyImpl implements ExecutableActionProperty {

    private final PropertySpec propertySpec;
    private final Object value;

    public ExecutableActionPropertyImpl(PropertySpec propertySpec, Object value) {
        super();
        this.propertySpec = propertySpec;
        this.value = value;
    }

    @Override
    public PropertySpec getPropertySpec() {
        return propertySpec;
    }

    @Override
    public Object getValue() {
        return value;
    }

}