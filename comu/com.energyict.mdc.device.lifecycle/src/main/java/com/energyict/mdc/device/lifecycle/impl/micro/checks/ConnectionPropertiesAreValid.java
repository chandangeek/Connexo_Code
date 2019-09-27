/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroCategory;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.upl.TypedProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        device.getDeviceConfiguration().getComTaskEnablements()
                .stream()
                .filter(ctEn->
                        device.getComTaskExecutions()
                                .stream()
                                .anyMatch(ctExec->ctEn.getComTask().getId()!=ctExec.getComTask().getId())
                ).map(device::newAdHocComTaskExecution);
        boolean defaultConnectionTaskContainsHP = device
                .getComTaskExecutions()
                .stream()
                .map(ComTaskExecution::getConnectionTask)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ConnectionTaskPropertyProvider::getTypedProperties)
                .anyMatch(ct-> ct.getStringProperty("host").equals("null") || ct.getStringProperty("portNumber").equals("null"));
        if(defaultConnectionTaskContainsHP){
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
