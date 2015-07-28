package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

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
public class DetachSlaveFromMaster extends TranslatableServerMicroAction {

    private final TopologyService topologyService;

    public DetachSlaveFromMaster(Thesaurus thesaurus, TopologyService topologyService) {
        super(thesaurus);
        this.topologyService = topologyService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        // Remember that effective timestamp is a required property enforced by the service's execute method
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        this.topologyService.getPhysicalGateway(device).ifPresent(master -> this.topologyService.clearPhysicalGateway(device));
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.DETACH_SLAVE_FROM_MASTER;
    }
}