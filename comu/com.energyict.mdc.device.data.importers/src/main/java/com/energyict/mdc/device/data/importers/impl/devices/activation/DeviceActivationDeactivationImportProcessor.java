package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Collections;
import java.util.List;

public class DeviceActivationDeactivationImportProcessor extends DeviceTransitionImportProcessor<DeviceActivationDeactivationRecord> {

    public DeviceActivationDeactivationImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected List<DefaultState> getSourceStates(DeviceActivationDeactivationRecord data) {
        return Collections.singletonList(data.isActivate() ? DefaultState.INACTIVE : DefaultState.ACTIVE);
    }

    @Override
    protected DefaultState getTargetState(DeviceActivationDeactivationRecord data) {
        return data.isActivate() ? DefaultState.ACTIVE : DefaultState.INACTIVE;
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
