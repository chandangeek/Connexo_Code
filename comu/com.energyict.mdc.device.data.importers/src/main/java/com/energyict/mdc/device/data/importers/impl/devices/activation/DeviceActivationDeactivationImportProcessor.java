/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.energyict.mdc.common.device.data.Device;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;

import java.util.Collections;
import java.util.List;

class DeviceActivationDeactivationImportProcessor extends DeviceTransitionImportProcessor<DeviceActivationDeactivationRecord> {

    DeviceActivationDeactivationImportProcessor(DeviceDataImporterContext context) {
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
