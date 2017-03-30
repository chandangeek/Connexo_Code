/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.decommission;

import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Arrays;
import java.util.List;

class DeviceDecommissioningImportProcessor extends DeviceTransitionImportProcessor<DeviceTransitionRecord> {

    DeviceDecommissioningImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected List<DefaultState> getSourceStates(DeviceTransitionRecord data) {
        return Arrays.asList(DefaultState.ACTIVE, DefaultState.INACTIVE);
    }

    @Override
    protected DefaultState getTargetState(DeviceTransitionRecord data) {
        return DefaultState.DECOMMISSIONED;
    }

    @Override
    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceTransitionRecord data) {
        return DefaultCustomStateTransitionEventType.DECOMMISSIONED;
    }
}
