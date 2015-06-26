package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will close the current meter activation for the Device on the effective
 * timestamp of the transition.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CLOSE_METER_ACTIVATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 5(14:30)
 */
public class CloseMeterActivation implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        // Remember that effective timestamp is a required property enforced by the service's execute method
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.deactivate(effectiveTimestamp);
    }

}