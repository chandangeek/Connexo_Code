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
import java.util.Map;
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

        boolean comTaskEnablementHasExecution = false;
        Map<String, ConnectionTask<?,?>> deviceConnectionTasks = device.getConnectionTasks().stream().collect(Collectors.toMap(ConnectionTask::getName, connectionTask->connectionTask));

        for(ComTaskEnablement comTaskEnablement:device.getDeviceConfiguration().getComTaskEnablements()){
            for(ComTaskExecution comTaskExecution:device.getComTaskExecutions()){
                if(comTaskEnablement.getComTask().getId() == comTaskExecution.getComTask().getId()) {
                    Optional<ConnectionTask<?,?>> connectionTask = comTaskExecution.getConnectionTask();
                    if (connectionTask.isPresent()) {
                        comTaskEnablementHasExecution = true;
                        if(connectionTask.get().getPluggableClass().getPropertySpecs().size()!=0){
                            return hasUnsetHostOrPort(connectionTask.get());
                        }
                        if(connectionTask.get().getProperties().stream()
                                .filter(ctp -> ctp.getValue() instanceof SecurityAccessorType)
                                .map(ctp -> (SecurityAccessorType) ctp.getValue())
                                .map(device::getSecurityAccessor)
                                .anyMatch(ka -> !ka.isPresent() || !ka.get().getActualPassphraseWrapperReference().isPresent())){
                            return true;
                        }
                        break;
                    }
                }else{
                    comTaskEnablementHasExecution = false;
                }
            }

            if(!comTaskEnablementHasExecution){
                Optional<PartialConnectionTask> connectionTask = comTaskEnablement.getPartialConnectionTask();
                if (connectionTask.isPresent()) {
                    if(connectionTask.get().getPluggableClass().getPropertySpecs().size()!=0){
                        //Com task enablement doesn't have filled typed properties unlike com task execution( or connection task from device )
                        return hasUnsetHostOrPort(deviceConnectionTasks.get(connectionTask.get().getName()));
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

    private boolean hasUnsetHostOrPort(ConnectionTask<?,?>  connectionTask){
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        return typedProperties.getTypedProperty("host") == null || typedProperties.getTypedProperty("portNumber") == null;
    }
}
