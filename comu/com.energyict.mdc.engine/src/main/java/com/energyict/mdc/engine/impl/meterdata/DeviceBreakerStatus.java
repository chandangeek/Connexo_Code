/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedBreakerStatusDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.Optional;

/**
 * Straightforward implementation of a BreakerStatus collectedData object.
 * By default the breaker status is empty.
 */
public class DeviceBreakerStatus extends CollectedDeviceData implements CollectedBreakerStatus {

    private final DeviceIdentifier deviceDeviceIdentifier;
    private Optional<BreakerStatus> breakerStatus = Optional.empty();
    private ComTaskExecution comTaskExecution;

    public DeviceBreakerStatus(DeviceIdentifier deviceIdentifier) {
        this.deviceDeviceIdentifier = deviceIdentifier;
    }

    @Override
    public Optional<BreakerStatus> getBreakerStatus() {
        return breakerStatus;
    }

    @Override
    public void setBreakerStatus(BreakerStatus breakerStatus) {
        this.breakerStatus = Optional.of(breakerStatus);
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {
        this.comTaskExecution = (ComTaskExecution) configuration;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceDeviceIdentifier;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedBreakerStatusDeviceCommand(serviceProvider, this, comTaskExecution);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToReadStatusInformation();
    }
}