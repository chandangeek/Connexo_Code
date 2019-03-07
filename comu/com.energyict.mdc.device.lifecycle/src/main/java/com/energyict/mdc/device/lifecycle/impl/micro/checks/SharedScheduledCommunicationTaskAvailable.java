/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that there is a ComTasksExecutions on the device
 */
public class SharedScheduledCommunicationTaskAvailable extends TranslatableServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp, State toState) {
        return !anyScheduledCommunicationTask(device).isPresent() ?
                fail(MicroCheckTranslations.Message.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE) :
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

    private Optional<ComTaskExecution> anyScheduledCommunicationTask(Device device) {
        return device
                .getComTaskExecutions()
                .stream()
                .filter(ComTaskExecution::usesSharedSchedule)
                .findAny();
    }
}
