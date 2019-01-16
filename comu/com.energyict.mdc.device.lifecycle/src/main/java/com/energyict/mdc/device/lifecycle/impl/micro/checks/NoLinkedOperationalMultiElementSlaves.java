/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import java.time.Instant;
import java.util.Optional;

/**
 * For multi-element devices make sure there are no slaves linked
 */
public class NoLinkedOperationalMultiElementSlaves extends TranslatableServerMicroCheck {

    private final MultiElementDeviceService multiElementDeviceService;

    public NoLinkedOperationalMultiElementSlaves(Thesaurus thesaurus, MultiElementDeviceService multiElementDeviceService) {
        super(thesaurus);
        this.multiElementDeviceService = multiElementDeviceService;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (hasLinkedOperationalDevices(device)) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.NO_LINKED_MULTI_ELEMENT_SLAVES,
                            getMicroCheck()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.NO_LINKED_MULTI_ELEMENT_SLAVES;
    }

    private boolean hasLinkedOperationalDevices(Device device) {
        return this.multiElementDeviceService.findMultiElementSlaves(device).stream()
                .filter(device1 -> device1.getState().getStage().get().getName().equals(EndDeviceStage.OPERATIONAL.getKey()))
                .findAny().isPresent();
    }


}