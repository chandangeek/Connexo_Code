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
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.upl.TypedProperties;

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

        boolean isHasExecution = false;
        for(ComTaskEnablement comTaskEnablement:device.getDeviceConfiguration().getComTaskEnablements()){
            for(ComTaskExecution comTaskExecution:device.getComTaskExecutions()){
                if(comTaskEnablement.getComTask().getId() == comTaskExecution.getComTask().getId()) {
                    Optional<ConnectionTask<?,?>> connectionTask = comTaskExecution.getConnectionTask();
                    if (connectionTask.isPresent()) {
                        isHasExecution = true;
                        if(isHasHostOrPort(connectionTask.get().getTypedProperties())){
                            return true;
                        }
                        if(connectionTask.get().getProperties().stream()
                                .filter(ctp -> ctp.getValue() instanceof SecurityAccessorType)
                                .map(ctp -> (SecurityAccessorType) ctp.getValue())
                                .map(device::getSecurityAccessor)
                                .anyMatch(ka -> !ka.isPresent() || !ka.get().getActualValue().isPresent())){
                            return true;
                        }
                        break;
                    }
                }else{
                    isHasExecution = false;
                }
            }
            if(!isHasExecution){
                Optional<PartialConnectionTask> connectionTask = comTaskEnablement.getPartialConnectionTask();
                if (connectionTask.isPresent()) {
                    if(isHasHostOrPort(connectionTask.get().getTypedProperties())){
                        return true;
                    }
                    if(connectionTask.get().getProperties().stream()
                            .filter(ctp -> ctp.getValue() instanceof SecurityAccessorType)
                            .map(ctp -> (SecurityAccessorType) ctp.getValue())
                            .map(device::getSecurityAccessor)
                            .anyMatch(ka -> !ka.isPresent() || !ka.get().getActualPassphraseWrapperReference().isPresent())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isHasHostOrPort(TypedProperties typedProperties){
        return typedProperties.getTypedProperty("host") == null || typedProperties.getTypedProperty("portNumber") == null;
    }
}
