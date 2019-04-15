/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.time.Instant;
import java.util.Optional;

/**
 * Checks that all the {@link ConnectionTask}s of a Device are complete.
 * In case a property is a KeyAccessorType, we also check the device has a value (KeyAccessor) for the KeyAccessorType
 * and the KeyAccessor has an actualValue
 */
public class ConnectionPropertiesAreValid extends ConsolidatedServerMicroCheck {

    @Override
    public String getCategory() {
        return MicroCategory.COMMUNICATION.name();
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
        return anyInCompleteConnectionTask(device) ?
                fail(MicroCheckTranslations.Message.CONNECTION_PROPERTIES_ARE_ALL_VALID) :
                Optional.empty();
    }

    private boolean anyInCompleteConnectionTask(Device device) {
        boolean containsIncompleteConnectionTask = device
                .getComTaskExecutions()
                .stream()
                .map(ComTaskExecution::getConnectionTask)
                .flatMap(Functions.asStream())
                .anyMatch(each -> each.getStatus().equals(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE));
        if (containsIncompleteConnectionTask) {
            return true;
        }
        return device
                .getComTaskExecutions()
                .stream()
                .map(ComTaskExecution::getConnectionTask)
                .flatMap(Functions.asStream())
                .flatMap(ct -> ct.getProperties().stream())
                .filter(ctp -> ctp.getValue() instanceof SecurityAccessorType)
                .map(ctp -> (SecurityAccessorType) ctp.getValue())
                .map(device::getSecurityAccessor)
                .anyMatch(ka -> !ka.isPresent() || !ka.get().getActualValue().isPresent());
    }
}
