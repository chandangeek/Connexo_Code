package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;

/**
 * Provides an implementation for the {@link DeviceLifeCycleBuilder} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (16:38)
 */
public class DeviceLifeCycleBuilderImpl implements DeviceLifeCycleBuilder {

    private final DeviceLifeCycleImpl underConstruction;

    public DeviceLifeCycleBuilderImpl(DeviceLifeCycleImpl underConstruction) {
        super();
        this.underConstruction = underConstruction;
    }

    @Override
    public DeviceLifeCycle complete() {
        return this.underConstruction;
    }

}