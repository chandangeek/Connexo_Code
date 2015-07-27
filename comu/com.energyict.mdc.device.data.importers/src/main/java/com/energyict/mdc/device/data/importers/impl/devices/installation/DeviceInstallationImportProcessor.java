package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
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
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeviceInstallationImportProcessor implements FileImportProcessor<DeviceInstallationImportRecord> {

    private final DeviceDataImporterContext context;

    public DeviceInstallationImportProcessor(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(DeviceInstallationImportRecord data, FileImportRecordContext recordContext) throws ProcessorException {
        Device device = this.context.getDeviceService().findByUniqueMrid(data.getDeviceMrid())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceMrid()));
        String targetStateName = getTargetStateName(data);
        if (targetStateName.equals(device.getState().getName())) {
            throw new ProcessorException(MessageSeeds.DEVICE_ALREADY_IN_THAT_STATE, data.getLineNumber(), recordContext.translate(targetStateName));
        }
        DefaultCustomStateTransitionEventType eventType = getTransitionEventType(data);
        ExecutableAction executableAction = this.context.getDeviceLifeCycleService().getExecutableActions(device,
                eventType.findOrCreate(this.context.getFiniteStateMachineService()))
                .orElseThrow(() -> new ProcessorException(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE, data.getLineNumber(),
                        recordContext.translate(targetStateName), recordContext.translate(device.getState().getName())));
        try {
//            executableAction.execute(data.getInstallationDate().toInstant(),
//                    getExecutableActionProperties(data, getAllPropertySpecsForAction(executableAction)));
        } catch (MultipleMicroCheckViolationsException ex){
            // TODO translate violations!
            throw new ProcessorException(MessageSeeds.PRE_TRANSITION_CHECKS_FAILED, data.getLineNumber(), "");
        }
        processMasterMrid(device, data, recordContext);
        processUsagePoint(device, data, recordContext);
        device.save();
    }

    private DefaultCustomStateTransitionEventType getTransitionEventType(DeviceInstallationImportRecord data) {
        return data.isInstallInactive() ? DefaultCustomStateTransitionEventType.DEACTIVATED
                : DefaultCustomStateTransitionEventType.ACTIVATED;
    }

    private String getTargetStateName(DeviceInstallationImportRecord data) {
        return data.isInstallInactive() ? DefaultState.INACTIVE.getKey() : DefaultState.ACTIVE.getKey();
    }

    private Map<String, PropertySpec> getAllPropertySpecsForAction(ExecutableAction executableAction) {
        if (executableAction.getAction() instanceof AuthorizedTransitionAction) {
            return ((AuthorizedTransitionAction) executableAction.getAction()).getActions()
                    .stream()
                    .flatMap(microAction -> context.getDeviceLifeCycleService().getPropertySpecsFor(microAction).stream())
                    .collect(Collectors.toMap(PropertySpec::getName, Function.<PropertySpec>identity(), (prop1, prop2) -> prop1));
        }
        return Collections.emptyMap();
    }

    private List<ExecutableActionProperty> getExecutableActionProperties(DeviceInstallationImportRecord data, Map<String, PropertySpec> allPropertySpecsForAction) {
        List<ExecutableActionProperty> executableProperties = new ArrayList<>(allPropertySpecsForAction.size());
        PropertySpec propertySpec = allPropertySpecsForAction.get(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key());
        if (propertySpec != null) {
            try {
                executableProperties.add(context.getDeviceLifeCycleService().toExecutableActionProperty(data.getStartValidationDate(), propertySpec));
            } catch (InvalidValueException e) {
                throw new ProcessorException(MessageSeeds.START_VALIDATION_DATE_IS_INCORRECT,
                        data.getLineNumber(), data.getStartValidationDate(), e.getLocalizedMessage());
            }
        }
        return executableProperties;
    }

    private void processMasterMrid(Device device, DeviceInstallationImportRecord data, FileImportRecordContext recordContext) {
        if (data.getMasterDeviceMrid() != null) {
            Device masterDevice = this.context.getDeviceService().findByUniqueMrid(data.getMasterDeviceMrid())
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
        this.context.getTopologyService().setPhysicalGateway(device, masterDevice);
    }

    private void processUsagePoint(Device device, DeviceInstallationImportRecord data, FileImportRecordContext recordContext) {
        if (data.getUsagePointMrid() != null) {
            Optional<UsagePoint> usagePointRef = this.context.getMeteringService().findUsagePoint(data.getUsagePointMrid());
            if (usagePointRef.isPresent()) {
                setUsagePoint(device, usagePointRef.get(), data);
            } else {
                // If not found, than create the light version of the usage point using usage point MRID + Service category
                recordContext.warning(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED, data.getLineNumber(), data.getUsagePointMrid());
                setUsagePoint(device, createNewUsagePoint(data), data);
            }
        }
    }

    private UsagePoint createNewUsagePoint(DeviceInstallationImportRecord data) {
        UsagePoint usagePoint = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.getDisplayName().equalsIgnoreCase(data.getServiceCategory()))
                .map(serviceKind -> this.context.getMeteringService().getServiceCategory(serviceKind))
                .findFirst()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_USAGE_POINT, data.getLineNumber(),
                        data.getUsagePointMrid(), Arrays.stream(ServiceKind.values()).map(ServiceKind::getDisplayName).collect(Collectors.joining(", "))))
                .newUsagePoint(data.getUsagePointMrid());
        usagePoint.save();
        return usagePoint;
    }

    private void setUsagePoint(Device device, UsagePoint usagePoint, DeviceInstallationImportRecord data) {
        this.context.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).ifPresent(amrSystem -> {
            amrSystem.findMeter(String.valueOf(device.getId())).ifPresent(meter -> {
                usagePoint.activate(meter, data.getInstallationDate().toInstant());
            });
        });
    }
}
