package com.energyict.mdc.device.data.importers.impl.devices;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.FileImportRecordContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract  class DeviceTransitionImportProcessor<T extends DeviceTransitionRecord> implements FileImportProcessor<T> {

    private final DeviceDataImporterContext context;

    public DeviceTransitionImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    protected DeviceDataImporterContext getContext() {
        return context;
    }

    @Override
    public void process(T data, FileImportRecordContext recordContext) throws ProcessorException {
        Device device = getContext().getDeviceService().findByUniqueMrid(data.getDeviceMrid())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceMrid()));
        beforeTransition(device, data, recordContext);
        performDeviceTransition(data, recordContext, device);
        processMasterMrid(device, data, recordContext);
        afterTransition(device, data, recordContext);
        device.save();
    }
    protected void beforeTransition(Device device, T data, FileImportRecordContext recordContext) throws ProcessorException {
        // do nothing
    }

    protected void afterTransition(Device device, T data, FileImportRecordContext recordContext) throws ProcessorException {
        // do nothing
    }

    private void performDeviceTransition(T data, FileImportRecordContext recordContext, Device device) {
        String targetStateName = getTargetStateName(data);
        if (targetStateName.equals(device.getState().getName())) {
            throw new ProcessorException(MessageSeeds.DEVICE_ALREADY_IN_THAT_STATE, data.getLineNumber(), recordContext.translate(targetStateName));
        }
        ExecutableAction executableAction = getExecutableAction(device, data, recordContext);
        try {
            executableAction.execute(data.getTransitionActionDate().orElse(getContext().getClock().instant()),
                    getExecutableActionProperties(data, getAllPropertySpecsForAction(executableAction)));
        } catch (MultipleMicroCheckViolationsException ex){
            throw new ProcessorException(MessageSeeds.PRE_TRANSITION_CHECKS_FAILED, data.getLineNumber(),
                    ex.getViolations()
                            .stream()
                            .map(violation -> context.getDeviceLifeCycleService().getName(violation.getCheck()))
                            .distinct()
                            .collect(Collectors.joining(", ")));
        }
    }

    protected abstract String getTargetStateName(T data);

    protected abstract DefaultCustomStateTransitionEventType getTransitionEventType(T data);

    private ExecutableAction getExecutableAction(Device device, T data, FileImportRecordContext recordContext) {
        DefaultCustomStateTransitionEventType eventType = getTransitionEventType(data);
        return getContext().getDeviceLifeCycleService().getExecutableActions(device,
                eventType.findOrCreate(this.context.getFiniteStateMachineService()))
                .orElseThrow(() -> new ProcessorException(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE, data.getLineNumber(),
                        recordContext.translate(getTargetStateName(data)), recordContext.translate(device.getState().getName())));
    }

    private Map<String, PropertySpec> getAllPropertySpecsForAction(ExecutableAction executableAction) {
        if (executableAction.getAction() instanceof AuthorizedTransitionAction) {
            return ((AuthorizedTransitionAction) executableAction.getAction()).getActions()
                    .stream()
                    .flatMap(microAction -> getContext().getDeviceLifeCycleService().getPropertySpecsFor(microAction).stream())
                    .collect(Collectors.toMap(PropertySpec::getName, Function.<PropertySpec>identity(), (prop1, prop2) -> prop1));
        }
        return Collections.emptyMap();
    }

    private List<ExecutableActionProperty> getExecutableActionProperties(T data, Map<String, PropertySpec> allPropertySpecsForAction) {
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

    private void processMasterMrid(Device device, T data, FileImportRecordContext recordContext) {
        if (data.getMasterDeviceMrid() != null) {
            Device masterDevice = getContext().getDeviceService().findByUniqueMrid(data.getMasterDeviceMrid())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_MASTER_DEVICE, data.getLineNumber(), data.getMasterDeviceMrid()));
            if (GatewayType.NONE.equals(masterDevice.getConfigurationGatewayType())) {
                throw new ProcessorException(MessageSeeds.DEVICE_CAN_NOT_BE_MASTER, data.getLineNumber(), masterDevice.getmRID());
            }
            Optional<Device> oldMasterDeviceRef = this.context.getTopologyService().getPhysicalGateway(device);
            if (oldMasterDeviceRef.isPresent()) {
                if (!oldMasterDeviceRef.get().getmRID().equals(masterDevice.getmRID())) {
                    recordContext.warning(TranslationKeys.MASTER_WILL_BE_OVERRIDDEN, data.getLineNumber(),
                            oldMasterDeviceRef.get().getmRID(), masterDevice.getmRID());
                    setNewMasterDevice(device, masterDevice);
                }
            } else {
                setNewMasterDevice(device, masterDevice);
            }
        }
    }

    private void setNewMasterDevice(Device device, Device masterDevice) {
        getContext().getTopologyService().setPhysicalGateway(device, masterDevice);
    }
}
