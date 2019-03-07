/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that a device is linked with a {@link com.elster.jupiter.metering.UsagePoint}.
 */
public class DeviceIsLinkedWithUsagePoint extends TranslatableServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.INSTALLATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp, State toState) {
        return !device.getUsagePoint().isPresent() ?
                fail(MicroCheckTranslations.Message.LINKED_WITH_USAGE_POINT) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_INACTIVE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_AND_ACTIVATE,
                DefaultTransition.INSTALL_INACTIVE,
                DefaultTransition.ACTIVATE);
    }
}
