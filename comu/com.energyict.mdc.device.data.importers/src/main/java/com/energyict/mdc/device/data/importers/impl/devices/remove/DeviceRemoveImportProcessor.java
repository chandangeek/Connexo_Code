/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.remove;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Arrays;
import java.util.List;

class DeviceRemoveImportProcessor extends DeviceTransitionImportProcessor<DeviceTransitionRecord> {

    DeviceRemoveImportProcessor(DeviceDataImporterContext context) {
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
}
