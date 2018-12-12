/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroCheck;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ServerMicroCheck} interface
 * that checks that all the {@link ConnectionTask}s of a Device are complete.
 * In case a property is a KeyAccessorType, we also check the device has a value (KeyAccessor) for the KeyAccessorType
 * and the KeyAccessor has an actualValue
 * @see ConnectionTask#getStatus()
 * @see ConnectionTask.ConnectionTaskLifecycleStatus#INCOMPLETE
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:48)
 */
public class ConnectionPropertiesAreValid extends ConsolidatedServerMicroCheck {

    public ConnectionPropertiesAreValid(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (anyInCompleteConnectionTask(device)) {
            return Optional.of(newViolation());
        }
        else {
            return Optional.empty();
        }
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
                .flatMap(ct->ct.getProperties().stream())
                .filter(ctp->ctp.getValue() instanceof SecurityAccessorType)
                .map(ctp-> (SecurityAccessorType)ctp.getValue())
                .map(device::getSecurityAccessor)
                .anyMatch(ka->!ka.isPresent() || !ka.get().getActualValue().isPresent());

    }

    private DeviceLifeCycleActionViolationImpl newViolation() {
        return new DeviceLifeCycleActionViolationImpl(
                this.thesaurus,
                MessageSeeds.CONNECTION_PROPERTIES_ARE_ALL_VALID,
                MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
    }

}