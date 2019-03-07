/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceStage;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * For multi-element devices make sure there are no slaves linked
 */
public class NoLinkedOperationalMultiElementSlaves extends TranslatableServerMicroCheck {

    private MultiElementDeviceService multiElementDeviceService;

    @Inject
    public final void setMultiElementDeviceService(MultiElementDeviceService multiElementDeviceService) {
        this.multiElementDeviceService = multiElementDeviceService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.MULTIELEMENT.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return hasLinkedOperationalDevices(device) ?
                fail(MicroCheckTranslations.Message.NO_LINKED_MULTI_ELEMENT_SLAVES) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.DEACTIVATE_AND_DECOMMISSION,
                DefaultTransition.DECOMMISSION);
    }

    private boolean hasLinkedOperationalDevices(Device device) {
        return this.multiElementDeviceService.findMultiElementSlaves(device)
                .stream()
                .anyMatch(device1 -> device1.getState().getStage().get().getName().equals(EndDeviceStage.OPERATIONAL.getKey()));
    }
}
