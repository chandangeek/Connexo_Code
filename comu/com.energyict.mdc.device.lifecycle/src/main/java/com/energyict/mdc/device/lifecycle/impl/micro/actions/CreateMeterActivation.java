package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will create a meter activation for the Device on the effective
 * timestamp of the transition.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CREATE_METER_ACTIVATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-2 5(14:19)
 */
public class CreateMeterActivation extends TranslatableServerMicroAction {

    public CreateMeterActivation(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        // Remember that effective timestamp is a required property enforced by the service's execute method
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.activate(effectiveTimestamp);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.CREATE_METER_ACTIVATION;
    }
}