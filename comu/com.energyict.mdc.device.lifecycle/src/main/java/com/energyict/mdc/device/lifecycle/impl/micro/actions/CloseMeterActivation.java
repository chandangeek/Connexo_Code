package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.getOptionalEffectiveTimestamp;
import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.optionalEffectiveTimestamp;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will close the current meter activation for the Device on the effective
 * timestamp of the transition.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CLOSE_METER_ACTIVATION}
 *
 * action bits: 128
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 5(14:30)
 */
public class CloseMeterActivation implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Arrays.asList(optionalEffectiveTimestamp(propertySpecService));
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        Optional<Instant> effectiveTimestamp = getOptionalEffectiveTimestamp(properties);
        if (effectiveTimestamp.isPresent()) {
            device.deactivate(effectiveTimestamp.get());
        }
        else {
            device.deactivateNow();
        }
    }

}