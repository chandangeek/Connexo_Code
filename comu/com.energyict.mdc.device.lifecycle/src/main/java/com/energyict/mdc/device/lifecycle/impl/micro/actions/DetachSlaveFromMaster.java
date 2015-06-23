package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import com.energyict.mdc.device.topology.TopologyService;

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
 * that will detach a slave Device from it physical master on the effective
 * timestamp of the transition.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#DETACH_SLAVE_FROM_MASTER}
 * <p>
 * action bits: 512
 * @since 2015-05-06 (15:11)
 */
public class DetachSlaveFromMaster implements ServerMicroAction {

    private final TopologyService topologyService;

    public DetachSlaveFromMaster(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Arrays.asList(optionalEffectiveTimestamp(propertySpecService));
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        this.topologyService.getPhysicalGateway(device).ifPresent(master -> this.topologyService.clearPhysicalGateway(device));
    }

}