package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.getLastCheckedTimestamp;
import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.lastCheckedTimestamp;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will enable validation on the Device.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#ENABLE_VALIDATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-20 (09:29)
 */
public class EnableValidation implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Arrays.asList(lastCheckedTimestamp(propertySpecService));
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.forValidation().activateValidation(getLastCheckedTimestamp(properties));
    }

}