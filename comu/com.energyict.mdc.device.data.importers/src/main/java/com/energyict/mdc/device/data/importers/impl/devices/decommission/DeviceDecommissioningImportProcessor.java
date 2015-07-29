package com.energyict.mdc.device.data.importers.impl.devices.decommission;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

public class DeviceDecommissioningImportProcessor extends DeviceTransitionImportProcessor<DeviceTransitionRecord> {

    public DeviceDecommissioningImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected String getTargetStateName(DeviceTransitionRecord data) {
        return DefaultState.DECOMMISSIONED.getKey();
    }

    @Override
    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceTransitionRecord data) {
        return DefaultCustomStateTransitionEventType.DECOMMISSIONED;
    }
}
