package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.List;

import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.effectiveTimestamp;
import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.getEffectiveTimestamp;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will create a meter activation for the Device on the effective
 * timestamp of the transition.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CREATE_METER_ACTIVATION}
 *
 * action bits: 64
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-2 5(14:19)
 */
public class CreateMeterActivation implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Arrays.asList(effectiveTimestamp(propertySpecService));
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        device.activate(getEffectiveTimestamp(properties));
    }

}