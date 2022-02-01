/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DeviceTransitionImportProcessor<T extends DeviceTransitionRecord> extends AbstractDeviceDataFileImportProcessor<T> {

    public DeviceTransitionImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void process(T data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        beforeTransition(device, data);
        performDeviceTransition(data, device, logger);
        processMasterMrid(device, data, logger);
        afterTransition(device, data, logger);
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    protected void beforeTransition(Device device, T data) throws ProcessorException {
    }

    protected void afterTransition(Device device, T data, FileImportLogger logger) throws ProcessorException {
    }

    private void performDeviceTransition(T data, Device device, FileImportLogger logger) {
        String targetStateName = getTargetState(data).getKey();
        if (targetStateName.equals(device.getState().getName())) {
            throw new ProcessorException(
                    MessageSeeds.DEVICE_ALREADY_IN_THAT_STATE,
                    data.getLineNumber(),
                    this.getStateName(data));
        }

        ExecutableAction executableAction = getExecutableAction(device, data);

        List<DefaultState> sourceStates = getSourceStates(data);
        // It's a temporary hotfix for CONM-2793,  TODO: implement well-designed solution for custom States
        String deviceState = device.getState().getName();
        if (!(sourceStates.stream().anyMatch(state -> state.getKey().equals(deviceState))
                || this.getClass().getName().toLowerCase().contains("installation")
                && (deviceState.equals("Auf Lager") || deviceState.equals("In Wartung")))) {
            throw new ProcessorException(
                    MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER,
                    data.getLineNumber(),
                    this.getStateName(data),
                    this.getStateName(device.getState()),
                    sourceStates
                            .stream()
                            .map(getContext().getMeteringTranslationService()::getDisplayName)
                            .collect(Collectors.joining(", ")));
        }

        try {
            List<ExecutableActionProperty> executableActionProperties = getExecutableActionProperties(data, getAllPropertySpecsForAction(executableAction), logger, executableAction);
            executableAction.execute(data.getTransitionDate().orElse(getContext().getClock().instant()),
                    executableActionProperties);
        } catch (MultipleMicroCheckViolationsException ex) {
            throw new ProcessorException(MessageSeeds.PRE_TRANSITION_CHECKS_FAILED, data.getLineNumber(),
                    ex.getViolations()
                            .stream()
                            .map(violation -> violation.getCheck().getName())
                            .distinct()
                            .collect(Collectors.joining(", ")));
        }
    }

    protected abstract List<DefaultState> getSourceStates(T data);

    protected abstract DefaultState getTargetState(T data);

    protected abstract DefaultCustomStateTransitionEventType getTransitionEventType(T data);

    private ExecutableAction getExecutableAction(Device device, T data) {
        DefaultCustomStateTransitionEventType eventType = getTransitionEventType(data);
        return getContext().getDeviceLifeCycleService().getExecutableActions(device,
                eventType.findOrCreate(getContext().getFiniteStateMachineService()))
                .orElseThrow(() -> new ProcessorException(
                        MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE,
                        data.getLineNumber(),
                        this.getStateName(data),
                        this.getStateName(device.getState())));
    }

    private String getStateName(T data) {
        return this.getContext().getMeteringTranslationService().getDisplayName(getTargetState(data));
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(getContext().getMeteringTranslationService()::getDisplayName)
                .orElseGet(state::getName);
    }

    private Map<String, PropertySpec> getAllPropertySpecsForAction(ExecutableAction executableAction) {
        if (executableAction.getAction() instanceof AuthorizedTransitionAction) {
            return ((AuthorizedTransitionAction) executableAction.getAction()).getActions()
                    .stream()
                    .flatMap(microAction -> getContext().getDeviceLifeCycleService().getPropertySpecsFor(microAction).stream())
                    .collect(Collectors.toMap(PropertySpec::getName, Function.identity(), (prop1, prop2) -> prop1));
        }
        return Collections.emptyMap();
    }

    protected List<ExecutableActionProperty> getExecutableActionProperties(T data, Map<String, PropertySpec> allPropertySpecsForAction, FileImportLogger logger, ExecutableAction executableAction) {
        List<ExecutableActionProperty> executableProperties = new ArrayList<>(allPropertySpecsForAction.size());
        PropertySpec propertySpec = allPropertySpecsForAction.get(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key());
        if (propertySpec != null) {
            try {
                executableProperties.add(getContext().getDeviceLifeCycleService()
                        .toExecutableActionProperty(data.getTransitionActionDate().orElse(getContext().getClock().instant()), propertySpec));
            } catch (InvalidValueException e) {
                throw new ProcessorException(MessageSeeds.TRANSITION_ACTION_DATE_IS_INCORRECT,
                        data.getLineNumber(), data.getTransitionActionDate(), e.getLocalizedMessage());
            }
        }
        return executableProperties;
    }

    private void processMasterMrid(Device device, T data, FileImportLogger logger) {
        if (data.getMasterDeviceIdentifier() != null) {
            Device newMasterDevice = findDeviceByIdentifier(data.getMasterDeviceIdentifier())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_MASTER_DEVICE, data.getLineNumber(), data.getMasterDeviceIdentifier()));
            if (GatewayType.NONE.equals(newMasterDevice.getConfigurationGatewayType())) {
                throw new ProcessorException(MessageSeeds.DEVICE_CAN_NOT_BE_MASTER, data.getLineNumber(), newMasterDevice.getName());
            }
            Optional<Device> currentMasterDevice = getContext().getTopologyService().getPhysicalGateway(device);
            if (currentMasterDevice.isPresent()) {
                if (!currentMasterDevice.get().equals(newMasterDevice)) {
                    logger.warning(TranslationKeys.MASTER_WILL_BE_OVERRIDDEN, data.getLineNumber(), currentMasterDevice.get().getName(), newMasterDevice.getName());
                    setNewMasterDevice(device, newMasterDevice);
                }
            } else {
                setNewMasterDevice(device, newMasterDevice);
            }
        }
    }

    private void setNewMasterDevice(Device device, Device masterDevice) {
        getContext().getTopologyService().setPhysicalGateway(device, masterDevice);
    }
}
