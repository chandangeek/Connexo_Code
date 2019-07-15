/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
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
 * Check if at least one successful communication task has been executed on the device.
 */
public class SuccessfulCommunicationTaskExecuted extends TranslatableServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return !anyExecutedComTask(device).isPresent() ?
                fail(MicroCheckTranslations.Message.AT_LEAST_ONE_SUCCESSFUL_COMMUNICATION_TASK_EXECUTED) :
                Optional.empty();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.allOf(DefaultTransition.class);
    }

    private Optional<ComTaskExecution> anyExecutedComTask(Device device) {
        return device
                .getComTaskExecutions()
                .stream()
                .filter(each -> each.getLastSuccessfulCompletionTimestamp() != null)
                .findAny();
    }
}
