/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that there is a default connection task on the device
 */
public class DefaultConnectionTaskAvailable extends TranslatableServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<EvaluableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp) {
        return !anyDefaultConnectionTask(device).isPresent() ?
                violationFailed(MicroCheckTranslationKeys.MICRO_CHECK_MESSAGE_DEFAULT_CONNECTION_AVAILABLE) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(
                DefaultTransition.COMMISSION,
                DefaultTransition.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_INACTIVE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_AND_ACTIVATE,
                DefaultTransition.INSTALL_INACTIVE,
                DefaultTransition.ACTIVATE);
    }

    private Optional<ConnectionTask<?, ?>> anyDefaultConnectionTask(Device device) {
        return device
                .getConnectionTasks()
                .stream()
                .filter(ConnectionTask::isDefault)
                .findAny();
    }
}