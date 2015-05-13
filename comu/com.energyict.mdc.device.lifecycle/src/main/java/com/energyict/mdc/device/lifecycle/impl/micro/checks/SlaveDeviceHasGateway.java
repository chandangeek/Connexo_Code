package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that a slave device has a gateway.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (10:41)
 */
public class SlaveDeviceHasGateway implements ServerMicroCheck {

    private final Thesaurus thesaurus;
    private final TopologyService topologyService;

    public SlaveDeviceHasGateway(Thesaurus thesaurus, TopologyService topologyService) {
        super();
        this.thesaurus = thesaurus;
        this.topologyService = topologyService;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (device.isLogicalSlave() && !this.hasGateway(device)) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.SLAVE_DEVICE_HAS_GATEWAY,
                            MicroCheck.SLAVE_DEVICE_HAS_GATEWAY));
        }
        else {
            return Optional.empty();
        }
    }

    private boolean hasGateway(Device device) {
        return this.topologyService.getPhysicalGateway(device).isPresent();
    }

}