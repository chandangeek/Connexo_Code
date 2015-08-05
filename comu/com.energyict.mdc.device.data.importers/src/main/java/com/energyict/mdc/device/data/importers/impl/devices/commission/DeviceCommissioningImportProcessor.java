package com.energyict.mdc.device.data.importers.impl.devices.commission;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Collections;
import java.util.List;

public class DeviceCommissioningImportProcessor extends DeviceTransitionImportProcessor<DeviceTransitionRecord> {

    public DeviceCommissioningImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected List<DefaultState> getSourceStates(DeviceTransitionRecord data) {
        return Collections.singletonList(DefaultState.IN_STOCK);
    }

    @Override
    protected DefaultState getTargetState(DeviceTransitionRecord data) {
        return DefaultState.COMMISSIONING;
    }

    @Override
    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceTransitionRecord data) {
        return DefaultCustomStateTransitionEventType.COMMISSIONING;
    }
}
