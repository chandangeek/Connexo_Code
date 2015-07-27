package com.energyict.mdc.device.data.importers.impl.devices.commission;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

public class DeviceCommissioningImportProcessor extends DeviceTransitionImportProcessor<DeviceTransitionRecord> {

    public DeviceCommissioningImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected String getTargetStateName(DeviceTransitionRecord data) {
        return DefaultState.COMMISSIONING.getKey();
    }

    @Override
    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceTransitionRecord data) {
        return DefaultCustomStateTransitionEventType.COMMISSIONING;
    }
}
