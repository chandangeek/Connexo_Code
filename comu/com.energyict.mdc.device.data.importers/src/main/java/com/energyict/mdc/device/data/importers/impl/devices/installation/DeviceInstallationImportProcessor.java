package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceInstallationImportProcessor extends DeviceTransitionImportProcessor<DeviceInstallationImportRecord> {

    public DeviceInstallationImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected void afterTransition(Device device, DeviceInstallationImportRecord data, FileImportLogger logger) throws ProcessorException {
        super.afterTransition(device, data, logger);
        processUsagePoint(device, data, logger);
    }

    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceInstallationImportRecord data) {
        return data.isInstallInactive() ? DefaultCustomStateTransitionEventType.DEACTIVATED
                : DefaultCustomStateTransitionEventType.ACTIVATED;
    }

    protected String getTargetStateName(DeviceInstallationImportRecord data) {
        return data.isInstallInactive() ? DefaultState.INACTIVE.getKey() : DefaultState.ACTIVE.getKey();
    }

    private void processUsagePoint(Device device, DeviceInstallationImportRecord data, FileImportLogger logger) {
        if (data.getUsagePointMrid() != null) {
            Optional<UsagePoint> usagePointRef = getContext().getMeteringService().findUsagePoint(data.getUsagePointMrid());
            if (usagePointRef.isPresent()) {
                setUsagePoint(device, usagePointRef.get(), data);
            } else {
                // If not found, than create the light version of the usage point using usage point MRID + Service category
                logger.warning(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED, data.getLineNumber(), data.getUsagePointMrid());
                setUsagePoint(device, createNewUsagePoint(data), data);
            }
        }
    }

    private UsagePoint createNewUsagePoint(DeviceInstallationImportRecord data) {
        UsagePoint usagePoint = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.getDisplayName().equalsIgnoreCase(data.getServiceCategory()))
                .map(serviceKind -> getContext().getMeteringService().getServiceCategory(serviceKind))
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
        getContext().getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).ifPresent(amrSystem -> {
            amrSystem.findMeter(String.valueOf(device.getId())).ifPresent(meter -> {
                usagePoint.activate(meter, data.getTransitionDate().orElse(getContext().getClock().instant()));
            });
        });
    }
}
