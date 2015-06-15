package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will disable estimation on the Device.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#DISABLE_ESTIMATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (08:43)
 */
public class DisableEstimation implements ServerMicroAction {

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        device.forEstimation().deactivateEstimation();
    }

}