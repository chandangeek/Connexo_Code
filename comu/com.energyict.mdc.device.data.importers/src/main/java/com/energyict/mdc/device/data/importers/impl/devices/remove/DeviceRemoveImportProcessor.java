package com.energyict.mdc.device.data.importers.impl.devices.remove;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Arrays;
import java.util.List;

public class DeviceRemoveImportProcessor extends DeviceTransitionImportProcessor<DeviceTransitionRecord> {

    public DeviceRemoveImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected List<DefaultState> getSourceStates(DeviceTransitionRecord data) {
        return Arrays.asList(DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED);
    }

    @Override
    protected DefaultState getTargetState(DeviceTransitionRecord data) {
        return DefaultState.REMOVED;
    }

    @Override
    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceTransitionRecord data) {
        return DefaultCustomStateTransitionEventType.REMOVED;
    }

    @Override
    protected void afterTransition(Device device, DeviceTransitionRecord data, FileImportLogger logger) throws ProcessorException {
        //empty because we don't want to save device since it is removed...
    }
}
