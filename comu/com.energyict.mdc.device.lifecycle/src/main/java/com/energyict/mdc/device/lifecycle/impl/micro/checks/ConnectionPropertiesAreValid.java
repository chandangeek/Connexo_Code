/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
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

    private final static String PROPERTY_SPEC_NAME_HOST = "host";
    private final static String PROPERTY_SPEC_NAME_PORT_NUMBER = "portNumber";

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
                        Map<String,PropertySpec> propertySpecs = connectionTask.get().getPluggableClass().getPropertySpecs().stream().collect(Collectors.toMap(PropertySpec::getName, propertySpec->propertySpec));
                        if(propertySpecs.size()!=0){
                            if(
                                Optional.ofNullable(propertySpecs.get(PROPERTY_SPEC_NAME_HOST)).isPresent() &&
                                Optional.ofNullable(propertySpecs.get(PROPERTY_SPEC_NAME_PORT_NUMBER)).isPresent() &&
                                hasUnsetHostOrPort(connectionTask.get())
                            ){
                                return true;
                            }
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
                    comTaskEnablementHasExecution = false;
                }
            }

            if(!comTaskEnablementHasExecution){
                Optional<PartialConnectionTask> connectionTask = comTaskEnablement.getPartialConnectionTask();
                if (connectionTask.isPresent()) {
                    Map<String,PropertySpec> propertySpecs = connectionTask.get().getPluggableClass().getPropertySpecs().stream().collect(Collectors.toMap(PropertySpec::getName, propertySpec->propertySpec));
                    if(propertySpecs.size()!=0){
                        //Com task enablement doesn't have filled typed properties unlike com task execution( or connection task from device )
                        Optional<ConnectionTask<?, ?>> devConnectionTask = Optional.ofNullable(deviceConnectionTasks.get(connectionTask.get().getName()));
                        if(
                            Optional.ofNullable(propertySpecs.get(PROPERTY_SPEC_NAME_HOST)).isPresent() &&
                            Optional.ofNullable(propertySpecs.get(PROPERTY_SPEC_NAME_PORT_NUMBER)).isPresent() &&
                            devConnectionTask.isPresent() &&
                            hasUnsetHostOrPort(devConnectionTask.get())
                        ){
                            return true;
                        }
                    }
                    if(connectionTask.get().getProperties().stream()
                            .filter(ctp -> ctp.getValue() instanceof SecurityAccessorType)
                            .map(ctp -> (SecurityAccessorType) ctp.getValue())
                            .map(device::getSecurityAccessor)
                            .anyMatch(ka -> !ka.isPresent() || !ka.get().getActualValue().isPresent())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasUnsetHostOrPort(ConnectionTask<?,?>  connectionTask){
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        return typedProperties.getTypedProperty(PROPERTY_SPEC_NAME_HOST) == null || typedProperties.getTypedProperty(PROPERTY_SPEC_NAME_PORT_NUMBER) == null;
    }
}
