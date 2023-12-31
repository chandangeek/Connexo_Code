/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.common.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Checks that there is a ComTaskExecution which has a schedule on the device
 */
public class ScheduledCommunicationTaskAvailable extends TranslatableServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return !anyManuallyScheduledCommunicationTask(device).isPresent() ?
                fail(MicroCheckTranslations.Message.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE) :
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

    private Optional<ComTaskExecution> anyManuallyScheduledCommunicationTask(Device device) {
        return Stream.concat(
                device
                        .getComTaskExecutions()
                        .stream()
                        .filter(ComTaskExecution::isScheduledManually)
                        .filter(Predicates.not(ComTaskExecution::isAdHoc)),
                device
                        .getComTaskExecutions()
                        .stream()
                        .filter(ComTaskExecution::usesSharedSchedule))
                .findAny();
    }
}
