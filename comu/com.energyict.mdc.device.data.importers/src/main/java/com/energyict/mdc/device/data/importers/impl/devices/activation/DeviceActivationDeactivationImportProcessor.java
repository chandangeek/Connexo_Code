package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

public class DeviceActivationDeactivationImportProcessor extends DeviceTransitionImportProcessor<DeviceActivationDeactivationRecord> {

    public DeviceActivationDeactivationImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected String getTargetStateName(DeviceActivationDeactivationRecord data) {
        return data.isActivate() ? DefaultState.ACTIVE.getKey() : DefaultState.INACTIVE.getKey();
    }

    @Override
    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceActivationDeactivationRecord data) {
        return data.isActivate() ? DefaultCustomStateTransitionEventType.ACTIVATED : DefaultCustomStateTransitionEventType.DEACTIVATED;
    }

    @Override
    protected void beforeTransition(Device device, DeviceActivationDeactivationRecord data) throws ProcessorException {
        super.beforeTransition(device, data);
        data.setTransitionActionDate(device.forValidation().getLastChecked().orElseGet(() ->
            device.getLifecycleDates().getInstalledDate().orElse(getContext().getClock().instant())));
    }
}
