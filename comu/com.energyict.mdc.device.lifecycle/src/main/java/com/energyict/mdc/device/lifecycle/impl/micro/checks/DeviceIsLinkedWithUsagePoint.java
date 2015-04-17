package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that a device is linked with a {@link com.elster.jupiter.metering.UsagePoint}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-17 (12:47)
 */
public class DeviceIsLinkedWithUsagePoint implements ServerMicroCheck {

    private final Thesaurus thesaurus;

    public DeviceIsLinkedWithUsagePoint(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device) {
        if (!device.getUsagePoint().isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.LINKED_WITH_USAGE_POINT,
                            MicroCheck.LINKED_WITH_USAGE_POINT));
        }
        else {
            return Optional.empty();
        }
    }

}